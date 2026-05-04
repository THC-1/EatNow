package com.eatnow.backend.favorite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.favorite.dto.FavoriteCreateRequest;
import com.eatnow.backend.favorite.entity.Favorite;
import com.eatnow.backend.favorite.mapper.FavoriteMapper;
import com.eatnow.backend.favorite.vo.FavoriteCheckVo;
import com.eatnow.backend.favorite.vo.FavoriteDishSummaryVo;
import com.eatnow.backend.favorite.vo.FavoriteItemVo;
import com.eatnow.backend.favorite.vo.FavoritePostSummaryVo;
import com.eatnow.backend.favorite.vo.FavoriteSummaryRelationVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private static final String TARGET_TYPE_DISH = "DISH";
    private static final String TARGET_TYPE_POST = "POST";

    private final FavoriteMapper favoriteMapper;

    @Transactional
    public Long createFavorite(FavoriteCreateRequest request) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        String targetType = normalizeTargetType(request.getTargetType());
        validateTargetExists(targetType, request.getTargetId());

        Favorite existingFavorite = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getTargetType, targetType)
                        .eq(Favorite::getTargetId, request.getTargetId())
                        .last("LIMIT 1")
        );
        if (existingFavorite != null) {
            return existingFavorite.getId();
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(request.getTargetId());
        favoriteMapper.insert(favorite);
        changeTargetFavoriteCount(targetType, request.getTargetId(), true);
        return favorite.getId();
    }

    @Transactional
    public void deleteFavorite(Long favoriteId) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        Favorite favorite = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getId, favoriteId)
                        .eq(Favorite::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (favorite == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "收藏记录不存在");
        }
        favoriteMapper.deleteById(favoriteId);
        changeTargetFavoriteCount(favorite.getTargetType(), favorite.getTargetId(), false);
    }

    public PageResult<FavoriteItemVo> listFavorites(String targetType, Integer page, Integer size) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        String normalizedTargetType = StringUtils.hasText(targetType) ? normalizeTargetType(targetType) : null;
        int currentPage = page == null ? 1 : page;
        int currentSize = size == null ? 10 : size;

        long total = favoriteMapper.countFavorites(userId, normalizedTargetType);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }

        long offset = (long) (currentPage - 1) * currentSize;
        List<FavoriteItemVo> records = favoriteMapper.selectFavoriteList(userId, normalizedTargetType, offset, currentSize);
        decorateFavorites(records);
        return PageResult.of(records, total, currentPage, currentSize);
    }

    public FavoriteCheckVo checkFavorite(String targetType, Long targetId) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        String normalizedTargetType = normalizeTargetType(targetType);
        Favorite favorite = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getTargetType, normalizedTargetType)
                        .eq(Favorite::getTargetId, targetId)
                        .last("LIMIT 1")
        );
        return new FavoriteCheckVo(favorite != null, favorite == null ? null : favorite.getId());
    }

    public void createFavoriteIfAbsent(Long userId, String targetType, Long targetId) {
        Favorite existingFavorite = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getTargetType, targetType)
                        .eq(Favorite::getTargetId, targetId)
                        .last("LIMIT 1")
        );
        if (existingFavorite != null) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        favoriteMapper.insert(favorite);
        changeTargetFavoriteCount(targetType, targetId, true);
    }

    private void validateTargetExists(String targetType, Long targetId) {
        if (targetId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "targetId must not be null");
        }
        Integer count = TARGET_TYPE_DISH.equals(targetType)
                ? favoriteMapper.countDishById(targetId)
                : favoriteMapper.countPostById(targetId);
        if (count == null || count == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "收藏目标不存在");
        }
    }

    private String normalizeTargetType(String targetType) {
        if (!StringUtils.hasText(targetType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "targetType must not be blank");
        }
        String normalized = targetType.trim().toUpperCase();
        if (!TARGET_TYPE_DISH.equals(normalized) && !TARGET_TYPE_POST.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅支持 DISH 或 POST 收藏");
        }
        return normalized;
    }

    private void decorateFavorites(List<FavoriteItemVo> records) {
        List<Long> dishIds = new ArrayList<>();
        List<Long> postIds = new ArrayList<>();
        for (FavoriteItemVo record : records) {
            if (TARGET_TYPE_DISH.equals(record.getTargetType())) {
                dishIds.add(record.getTargetId());
            } else if (TARGET_TYPE_POST.equals(record.getTargetType())) {
                postIds.add(record.getTargetId());
            }
        }

        Map<Long, FavoriteDishSummaryVo> dishMap = buildDishMap(dishIds);
        Map<Long, FavoritePostSummaryVo> postMap = buildPostMap(postIds);
        for (FavoriteItemVo record : records) {
            if (TARGET_TYPE_DISH.equals(record.getTargetType())) {
                record.setDish(dishMap.get(record.getTargetId()));
            } else if (TARGET_TYPE_POST.equals(record.getTargetType())) {
                record.setPost(postMap.get(record.getTargetId()));
            }
        }
    }

    private Map<Long, FavoriteDishSummaryVo> buildDishMap(List<Long> dishIds) {
        if (dishIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, FavoriteDishSummaryVo> result = new LinkedHashMap<>();
        for (FavoriteSummaryRelationVo relation : favoriteMapper.selectDishSummaries(dishIds)) {
            FavoriteDishSummaryVo summaryVo = result.computeIfAbsent(relation.getId(), key -> {
                FavoriteDishSummaryVo vo = new FavoriteDishSummaryVo();
                vo.setId(relation.getId());
                vo.setName(relation.getTitle());
                vo.setPrice(relation.getPrice());
                vo.setScore(relation.getScore());
                vo.setImages(new ArrayList<>());
                return vo;
            });
            if (StringUtils.hasText(relation.getImageUrl())) {
                summaryVo.getImages().add(relation.getImageUrl());
            }
        }
        return result;
    }

    private Map<Long, FavoritePostSummaryVo> buildPostMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, FavoritePostSummaryVo> result = new LinkedHashMap<>();
        for (FavoriteSummaryRelationVo relation : favoriteMapper.selectPostSummaries(postIds)) {
            FavoritePostSummaryVo summaryVo = result.computeIfAbsent(relation.getId(), key -> {
                FavoritePostSummaryVo vo = new FavoritePostSummaryVo();
                vo.setId(relation.getId());
                vo.setTitle(relation.getTitle());
                vo.setDishName(relation.getDishName());
                vo.setPrice(relation.getPrice());
                vo.setScore(relation.getScore());
                vo.setImages(new ArrayList<>());
                return vo;
            });
            if (StringUtils.hasText(relation.getImageUrl())) {
                summaryVo.getImages().add(relation.getImageUrl());
            }
        }
        return result;
    }

    private void changeTargetFavoriteCount(String targetType, Long targetId, boolean increase) {
        if (TARGET_TYPE_DISH.equals(targetType)) {
            if (increase) {
                favoriteMapper.increaseDishFavoriteCount(targetId);
            } else {
                favoriteMapper.decreaseDishFavoriteCount(targetId);
            }
            return;
        }

        if (increase) {
            favoriteMapper.increasePostFavoriteCount(targetId);
        } else {
            favoriteMapper.decreasePostFavoriteCount(targetId);
        }
    }
}
