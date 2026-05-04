package com.eatnow.backend.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.review.dto.ReviewCreateRequest;
import com.eatnow.backend.review.entity.Review;
import com.eatnow.backend.review.entity.ReviewImage;
import com.eatnow.backend.review.entity.ReviewLike;
import com.eatnow.backend.review.mapper.ReviewImageMapper;
import com.eatnow.backend.review.mapper.ReviewLikeMapper;
import com.eatnow.backend.review.mapper.ReviewMapper;
import com.eatnow.backend.review.vo.ReviewAggregateVo;
import com.eatnow.backend.review.vo.ReviewImageRelationVo;
import com.eatnow.backend.review.vo.ReviewItemVo;
import com.eatnow.backend.review.vo.ReviewLikeCountVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final String TARGET_TYPE_DISH = "DISH";
    private static final String STATUS_VISIBLE = "VISIBLE";
    private static final String STATUS_DELETED = "DELETED";

    private final ReviewMapper reviewMapper;
    private final ReviewImageMapper reviewImageMapper;
    private final ReviewLikeMapper reviewLikeMapper;

    @Transactional
    public Long createReview(ReviewCreateRequest request) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        Long dishId = normalizeDishTarget(request.getTargetType(), request.getTargetId());
        ensureDishVisible(dishId);

        Review existingReview = reviewMapper.selectOne(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getUserId, userId)
                        .eq(Review::getDishId, dishId)
                        .last("LIMIT 1")
        );
        if (existingReview != null && !STATUS_DELETED.equals(existingReview.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "你已经评价过这道菜了");
        }

        if (existingReview == null) {
            Review review = buildReview(userId, dishId, request);
            reviewMapper.insert(review);
            replaceImages(review.getId(), request.getImages());
            refreshDishAndMerchantStats(dishId);
            return review.getId();
        }

        reviewMapper.update(
                null,
                new LambdaUpdateWrapper<Review>()
                        .eq(Review::getId, existingReview.getId())
                        .set(Review::getTargetType, TARGET_TYPE_DISH)
                        .set(Review::getOverallScore, request.getOverallScore())
                        .set(Review::getTasteScore, request.getTasteScore())
                        .set(Review::getPortionScore, request.getPortionScore())
                        .set(Review::getValueScore, request.getValueScore())
                        .set(Review::getContent, request.getContent())
                        .set(Review::getIsAnonymous, Boolean.TRUE.equals(request.getIsAnonymous()))
                        .set(Review::getLikeCount, 0)
                        .set(Review::getStatus, STATUS_VISIBLE)
        );
        reviewLikeMapper.delete(new LambdaQueryWrapper<ReviewLike>().eq(ReviewLike::getReviewId, existingReview.getId()));
        replaceImages(existingReview.getId(), request.getImages());
        refreshDishAndMerchantStats(dishId);
        return existingReview.getId();
    }

    public PageResult<ReviewItemVo> listReviews(String targetType, Long targetId, String sortBy, Integer page, Integer size) {
        Long dishId = normalizeDishTarget(targetType, targetId);
        int currentPage = page == null ? 1 : page;
        int currentSize = size == null ? 10 : size;
        String normalizedSort = normalizeSort(sortBy);
        long total = reviewMapper.countReviewList(dishId, STATUS_VISIBLE, null);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }
        long offset = (long) (currentPage - 1) * currentSize;
        List<ReviewItemVo> records = reviewMapper.selectReviewList(dishId, STATUS_VISIBLE, normalizedSort, offset, currentSize);
        decorate(records, SecurityUtils.getCurrentUserOrNull() == null ? null : SecurityUtils.getCurrentUserId());
        return PageResult.of(records, total, currentPage, currentSize);
    }

    public PageResult<ReviewItemVo> listMyReviews(Integer page, Integer size) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        int currentPage = page == null ? 1 : page;
        int currentSize = size == null ? 10 : size;
        long total = reviewMapper.countMyReviewList(userId);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }
        long offset = (long) (currentPage - 1) * currentSize;
        List<ReviewItemVo> records = reviewMapper.selectMyReviewList(userId, offset, currentSize);
        decorate(records, userId);
        return PageResult.of(records, total, currentPage, currentSize);
    }

    @Transactional
    public ReviewLikeCountVo likeReview(Long reviewId) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        Review review = getVisibleReview(reviewId);

        ReviewLike existingLike = reviewLikeMapper.selectOne(
                new LambdaQueryWrapper<ReviewLike>()
                        .eq(ReviewLike::getReviewId, reviewId)
                        .eq(ReviewLike::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (existingLike == null) {
            ReviewLike reviewLike = new ReviewLike();
            reviewLike.setReviewId(reviewId);
            reviewLike.setUserId(userId);
            reviewLikeMapper.insert(reviewLike);
            reviewMapper.increaseReviewLikeCount(reviewId);
            review.setLikeCount((review.getLikeCount() == null ? 0 : review.getLikeCount()) + 1);
        }
        return new ReviewLikeCountVo(review.getLikeCount());
    }

    @Transactional
    public ReviewLikeCountVo unlikeReview(Long reviewId) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        Review review = getVisibleReview(reviewId);

        int deleted = reviewLikeMapper.delete(
                new LambdaQueryWrapper<ReviewLike>()
                        .eq(ReviewLike::getReviewId, reviewId)
                        .eq(ReviewLike::getUserId, userId)
        );
        if (deleted > 0) {
            reviewMapper.decreaseReviewLikeCount(reviewId);
            int currentLikeCount = review.getLikeCount() == null ? 0 : review.getLikeCount();
            review.setLikeCount(Math.max(0, currentLikeCount - 1));
        }
        return new ReviewLikeCountVo(review.getLikeCount());
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Review review = reviewMapper.selectById(reviewId);
        if (review == null || STATUS_DELETED.equals(review.getStatus())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "评价不存在");
        }
        boolean isOwner = userId.equals(review.getUserId());
        boolean isAdmin = SecurityUtils.hasRole(RoleCode.ADMIN);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权删除该评价");
        }

        reviewMapper.update(
                null,
                new LambdaUpdateWrapper<Review>()
                        .eq(Review::getId, reviewId)
                        .set(Review::getStatus, STATUS_DELETED)
        );
        refreshDishAndMerchantStats(review.getDishId());
    }

    private void decorate(List<ReviewItemVo> records, Long currentUserId) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> reviewIds = records.stream().map(ReviewItemVo::getId).toList();
        Map<Long, List<String>> imageMap = buildImageMap(reviewIds);
        Set<Long> likedReviewIds = currentUserId == null
                ? Collections.emptySet()
                : reviewMapper.selectLikedReviewIds(reviewIds, currentUserId).stream().collect(Collectors.toSet());

        for (ReviewItemVo record : records) {
            record.setImages(imageMap.getOrDefault(record.getId(), Collections.emptyList()));
            record.setIsLiked(likedReviewIds.contains(record.getId()));
            if (Boolean.TRUE.equals(record.getIsAnonymous()) && (currentUserId == null || !currentUserId.equals(record.getUserId()))) {
                record.setUserNickname("匿名用户");
                record.setUserAvatar(null);
            }
        }
    }

    private Map<Long, List<String>> buildImageMap(List<Long> reviewIds) {
        List<ReviewImageRelationVo> relations = reviewMapper.selectReviewImages(reviewIds);
        Map<Long, List<String>> imageMap = new LinkedHashMap<>();
        for (ReviewImageRelationVo relation : relations) {
            imageMap.computeIfAbsent(relation.getReviewId(), key -> new ArrayList<>()).add(relation.getImageUrl());
        }
        return imageMap;
    }

    private Review buildReview(Long userId, Long dishId, ReviewCreateRequest request) {
        Review review = new Review();
        review.setUserId(userId);
        review.setDishId(dishId);
        review.setTargetType(TARGET_TYPE_DISH);
        review.setOverallScore(request.getOverallScore());
        review.setTasteScore(request.getTasteScore());
        review.setPortionScore(request.getPortionScore());
        review.setValueScore(request.getValueScore());
        review.setContent(request.getContent());
        review.setIsAnonymous(Boolean.TRUE.equals(request.getIsAnonymous()));
        review.setLikeCount(0);
        review.setStatus(STATUS_VISIBLE);
        return review;
    }

    private void replaceImages(Long reviewId, List<String> images) {
        reviewImageMapper.delete(new LambdaQueryWrapper<ReviewImage>().eq(ReviewImage::getReviewId, reviewId));
        if (images == null || images.isEmpty()) {
            return;
        }
        for (int i = 0; i < images.size(); i++) {
            String imageUrl = images.get(i);
            if (!StringUtils.hasText(imageUrl)) {
                continue;
            }
            ReviewImage reviewImage = new ReviewImage();
            reviewImage.setReviewId(reviewId);
            reviewImage.setImageUrl(imageUrl.trim());
            reviewImage.setSortOrder(i);
            reviewImageMapper.insert(reviewImage);
        }
    }

    private void refreshDishAndMerchantStats(Long dishId) {
        ReviewAggregateVo dishAggregate = reviewMapper.selectDishReviewAggregate(dishId);
        ReviewAggregateVo normalizedDishAggregate = normalizeAggregate(dishAggregate);
        reviewMapper.updateDishReviewStats(
                dishId,
                normalizedDishAggregate.getReviewCount(),
                normalizedDishAggregate.getAverageScore(),
                normalizedDishAggregate.getTasteScore(),
                normalizedDishAggregate.getPortionScore(),
                normalizedDishAggregate.getValueScore()
        );

        Long merchantId = reviewMapper.selectMerchantIdByDishId(dishId);
        if (merchantId == null) {
            return;
        }
        ReviewAggregateVo merchantAggregate = normalizeAggregate(reviewMapper.selectMerchantReviewAggregate(merchantId));
        reviewMapper.updateMerchantReviewStats(
                merchantId,
                merchantAggregate.getReviewCount(),
                merchantAggregate.getAverageScore()
        );
    }

    private ReviewAggregateVo normalizeAggregate(ReviewAggregateVo aggregateVo) {
        ReviewAggregateVo normalized = new ReviewAggregateVo();
        normalized.setReviewCount(aggregateVo == null || aggregateVo.getReviewCount() == null ? 0 : aggregateVo.getReviewCount());
        normalized.setAverageScore(aggregateVo == null || aggregateVo.getAverageScore() == null ? BigDecimal.ZERO : aggregateVo.getAverageScore());
        normalized.setTasteScore(aggregateVo == null || aggregateVo.getTasteScore() == null ? BigDecimal.ZERO : aggregateVo.getTasteScore());
        normalized.setPortionScore(aggregateVo == null || aggregateVo.getPortionScore() == null ? BigDecimal.ZERO : aggregateVo.getPortionScore());
        normalized.setValueScore(aggregateVo == null || aggregateVo.getValueScore() == null ? BigDecimal.ZERO : aggregateVo.getValueScore());
        return normalized;
    }

    private Review getVisibleReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null || !STATUS_VISIBLE.equals(review.getStatus())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "评价不存在");
        }
        return review;
    }

    private Long normalizeDishTarget(String targetType, Long targetId) {
        if (!StringUtils.hasText(targetType) || !TARGET_TYPE_DISH.equals(targetType.trim().toUpperCase())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "当前仅支持 DISH 评价");
        }
        if (targetId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "targetId must not be null");
        }
        return targetId;
    }

    private String normalizeSort(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "latest";
        }
        String normalized = sortBy.trim().toLowerCase();
        if (!"latest".equals(normalized) && !"highest".equals(normalized) && !"lowest".equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "sortBy is invalid");
        }
        return normalized;
    }

    private void ensureDishVisible(Long dishId) {
        Integer count = reviewMapper.countVisibleDishById(dishId);
        if (count == null || count == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "菜品不存在或不可评价");
        }
    }
}
