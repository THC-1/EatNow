package com.eatnow.backend.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.post.dto.PostCreateRequest;
import com.eatnow.backend.post.entity.StudentPost;
import com.eatnow.backend.post.entity.StudentPostLike;
import com.eatnow.backend.post.mapper.StudentPostLikeMapper;
import com.eatnow.backend.post.mapper.StudentPostMapper;
import com.eatnow.backend.post.vo.PostImageRelationVo;
import com.eatnow.backend.post.vo.PostItemVo;
import com.eatnow.backend.post.vo.PostLikeCountVo;
import com.eatnow.backend.post.vo.PostTagRelationVo;
import com.eatnow.backend.post.vo.PostTagVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_DELETED = "DELETED";
    private static final String LOTTERY_POOL_STATUS_INACTIVE = "INACTIVE";

    private final StudentPostMapper studentPostMapper;
    private final StudentPostLikeMapper studentPostLikeMapper;

    @Transactional
    public Long createPost(PostCreateRequest request) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();

        String foodName = requireText(request.getFoodName(), "foodName");
        String shopName = requireText(request.getShopName(), "shopName");
        String content = requireText(request.getContent(), "content");
        String title = StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : foodName;
        validateReferences(request.getCanteenId(), request.getStallId(), request.getCategoryId());
        List<Long> tagIds = normalizeTagIds(request.getTagIds());
        List<String> images = normalizeImages(request.getImages());

        StudentPost post = new StudentPost();
        post.setUserId(userId);
        post.setCanteenId(request.getCanteenId());
        post.setStallId(request.getStallId());
        post.setCategoryId(request.getCategoryId());
        post.setTitle(title);
        post.setDishName(foodName);
        post.setShopName(shopName);
        post.setContent(content);
        post.setPrice(request.getPrice());
        post.setScore(request.getScore());
        post.setCoverImageUrl(firstImageOrNull(images));
        post.setIsJoinLottery(Boolean.TRUE.equals(request.getIsJoinLottery()));
        post.setStatus(STATUS_PUBLISHED);
        studentPostMapper.insert(post);

        replaceImages(post.getId(), images);
        replaceTags(post.getId(), tagIds);
        studentPostMapper.upsertPostLotteryPool(post.getId());
        return post.getId();
    }

    public PageResult<PostItemVo> listPosts(
            String keyword,
            Long canteenId,
            Long stallId,
            Long categoryId,
            Long tagId,
            String sortBy,
            Integer page,
            Integer size
    ) {
        int currentPage = page == null ? 1 : page;
        int currentSize = size == null ? 10 : size;
        String normalizedKeyword = trimToNull(keyword);
        String normalizedSort = normalizeSort(sortBy);
        long total = studentPostMapper.countPosts(normalizedKeyword, canteenId, stallId, categoryId, tagId);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }

        long offset = (long) (currentPage - 1) * currentSize;
        List<PostItemVo> records = studentPostMapper.selectPostList(
                normalizedKeyword,
                canteenId,
                stallId,
                categoryId,
                tagId,
                normalizedSort,
                offset,
                currentSize
        );
        decorate(records, currentUserIdOrNull());
        return PageResult.of(records, total, currentPage, currentSize);
    }

    public PostItemVo getPostDetail(Long postId) {
        PostItemVo detail = studentPostMapper.selectPostDetail(postId);
        if (detail == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Post not found");
        }
        studentPostMapper.increaseViewCount(postId);
        detail.setViewCount(detail.getViewCount() == null ? 1 : detail.getViewCount() + 1);
        decorate(List.of(detail), currentUserIdOrNull());
        return detail;
    }

    public PageResult<PostItemVo> listMyPosts(Integer page, Integer size) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        int currentPage = page == null ? 1 : page;
        int currentSize = size == null ? 10 : size;
        long total = studentPostMapper.countMyPosts(userId);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }

        long offset = (long) (currentPage - 1) * currentSize;
        List<PostItemVo> records = studentPostMapper.selectMyPostList(userId, offset, currentSize);
        decorate(records, userId);
        return PageResult.of(records, total, currentPage, currentSize);
    }

    @Transactional
    public PostLikeCountVo likePost(Long postId) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        StudentPost post = getPublishedPost(postId);

        StudentPostLike existingLike = studentPostLikeMapper.selectOne(
                new LambdaQueryWrapper<StudentPostLike>()
                        .eq(StudentPostLike::getPostId, postId)
                        .eq(StudentPostLike::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (existingLike == null) {
            StudentPostLike postLike = new StudentPostLike();
            postLike.setPostId(postId);
            postLike.setUserId(userId);
            studentPostLikeMapper.insert(postLike);
            studentPostMapper.increaseLikeCount(postId);
            post.setLikeCount((post.getLikeCount() == null ? 0 : post.getLikeCount()) + 1);
        }
        return new PostLikeCountVo(post.getLikeCount());
    }

    @Transactional
    public PostLikeCountVo unlikePost(Long postId) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        StudentPost post = getPublishedPost(postId);

        int deleted = studentPostLikeMapper.delete(
                new LambdaQueryWrapper<StudentPostLike>()
                        .eq(StudentPostLike::getPostId, postId)
                        .eq(StudentPostLike::getUserId, userId)
        );
        if (deleted > 0) {
            studentPostMapper.decreaseLikeCount(postId);
            int currentLikeCount = post.getLikeCount() == null ? 0 : post.getLikeCount();
            post.setLikeCount(Math.max(0, currentLikeCount - 1));
        }
        return new PostLikeCountVo(post.getLikeCount());
    }

    @Transactional
    public void deletePost(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        StudentPost post = studentPostMapper.selectById(postId);
        if (post == null || STATUS_DELETED.equals(post.getStatus())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Post not found");
        }
        boolean isOwner = userId.equals(post.getUserId());
        boolean isAdmin = SecurityUtils.hasRole(RoleCode.ADMIN);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No permission to delete this post");
        }

        studentPostMapper.update(
                null,
                new LambdaUpdateWrapper<StudentPost>()
                        .eq(StudentPost::getId, postId)
                        .set(StudentPost::getStatus, STATUS_DELETED)
        );
        studentPostMapper.updatePostLotteryPoolStatus(postId, LOTTERY_POOL_STATUS_INACTIVE);
    }

    private void decorate(List<PostItemVo> records, Long currentUserId) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> postIds = records.stream().map(PostItemVo::getId).toList();
        Map<Long, List<String>> imageMap = buildImageMap(postIds);
        Map<Long, List<PostTagVo>> tagMap = buildTagMap(postIds);
        Set<Long> likedPostIds = currentUserId == null
                ? Collections.emptySet()
                : studentPostMapper.selectLikedPostIds(postIds, currentUserId).stream().collect(Collectors.toSet());

        for (PostItemVo record : records) {
            record.setImages(imageMap.getOrDefault(record.getId(), Collections.emptyList()));
            record.setTags(tagMap.getOrDefault(record.getId(), Collections.emptyList()));
            record.setIsLiked(likedPostIds.contains(record.getId()));
        }
    }

    private Map<Long, List<String>> buildImageMap(List<Long> postIds) {
        List<PostImageRelationVo> relations = studentPostMapper.selectPostImages(postIds);
        Map<Long, List<String>> imageMap = new LinkedHashMap<>();
        for (PostImageRelationVo relation : relations) {
            imageMap.computeIfAbsent(relation.getPostId(), key -> new ArrayList<>()).add(relation.getImageUrl());
        }
        return imageMap;
    }

    private Map<Long, List<PostTagVo>> buildTagMap(List<Long> postIds) {
        List<PostTagRelationVo> relations = studentPostMapper.selectPostTags(postIds);
        return relations.stream().collect(Collectors.groupingBy(
                PostTagRelationVo::getPostId,
                LinkedHashMap::new,
                Collectors.mapping(
                        relation -> new PostTagVo(relation.getTagId(), relation.getTagName()),
                        Collectors.toList()
                )
        ));
    }

    private StudentPost getPublishedPost(Long postId) {
        StudentPost post = studentPostMapper.selectById(postId);
        if (post == null || !STATUS_PUBLISHED.equals(post.getStatus())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Post not found");
        }
        return post;
    }

    private void validateReferences(Long canteenId, Long stallId, Long categoryId) {
        if (canteenId != null && studentPostMapper.countOpenCanteenById(canteenId) == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "canteenId is invalid");
        }
        if (stallId != null && studentPostMapper.countOpenStallById(stallId, canteenId) == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "stallId is invalid");
        }
        if (categoryId != null && studentPostMapper.countActiveCategoryById(categoryId) == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "categoryId is invalid");
        }
    }

    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> uniqueIds = tagIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (uniqueIds.size() != tagIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "tagIds is invalid");
        }
        List<Long> normalizedTagIds = new ArrayList<>(uniqueIds);
        if (studentPostMapper.countActiveTagsByIds(normalizedTagIds) != normalizedTagIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "tagIds is invalid");
        }
        return normalizedTagIds;
    }

    private List<String> normalizeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (String image : images) {
            String trimmed = trimToNull(image);
            if (trimmed == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "images must not contain blank url");
            }
            if (trimmed.length() > 255) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "image url length must be less than or equal to 255");
            }
            normalized.add(trimmed);
        }
        return normalized;
    }

    private void replaceImages(Long postId, List<String> images) {
        studentPostMapper.deletePostImages(postId);
        if (!images.isEmpty()) {
            studentPostMapper.insertPostImages(postId, images);
        }
    }

    private void replaceTags(Long postId, List<Long> tagIds) {
        studentPostMapper.deletePostTags(postId);
        if (!tagIds.isEmpty()) {
            studentPostMapper.insertPostTags(postId, tagIds);
        }
    }

    private String normalizeSort(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "latest";
        }
        String normalized = sortBy.trim().toLowerCase();
        if (!"latest".equals(normalized) && !"popular".equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "sortBy is invalid");
        }
        return normalized;
    }

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String firstImageOrNull(List<String> images) {
        return images.isEmpty() ? null : images.get(0);
    }

    private Long currentUserIdOrNull() {
        return SecurityUtils.getCurrentUserOrNull() == null ? null : SecurityUtils.getCurrentUserId();
    }
}
