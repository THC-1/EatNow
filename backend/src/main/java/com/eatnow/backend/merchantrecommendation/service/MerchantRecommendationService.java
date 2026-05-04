package com.eatnow.backend.merchantrecommendation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.dish.entity.Dish;
import com.eatnow.backend.dish.mapper.DishMapper;
import com.eatnow.backend.dish.vo.DishImageRelationVo;
import com.eatnow.backend.merchant.entity.Merchant;
import com.eatnow.backend.merchant.mapper.MerchantMapper;
import com.eatnow.backend.merchantrecommendation.dto.MerchantRecommendationCreateRequest;
import com.eatnow.backend.merchantrecommendation.dto.MerchantRecommendationUpdateRequest;
import com.eatnow.backend.merchantrecommendation.entity.MerchantRecommendation;
import com.eatnow.backend.merchantrecommendation.mapper.MerchantRecommendationMapper;
import com.eatnow.backend.merchantrecommendation.vo.DishRecommendationVo;
import com.eatnow.backend.merchantrecommendation.vo.MerchantRecommendationItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MerchantRecommendationService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final Set<String> RECOMMEND_TYPES = Set.of("TODAY", "NEW", "SPECIAL", "VALUE", "SIGNATURE");
    private static final Set<String> RECOMMEND_STATUSES = Set.of(STATUS_ACTIVE, STATUS_EXPIRED, STATUS_CANCELLED);

    private final MerchantRecommendationMapper merchantRecommendationMapper;
    private final MerchantMapper merchantMapper;
    private final DishMapper dishMapper;

    @Transactional
    public Long createRecommendation(MerchantRecommendationCreateRequest request) {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
        Long merchantId = resolveCurrentMerchantId();
        validateDishOwnership(merchantId, request.getDishId());
        validateTimeRange(request.getStartTime(), request.getEndTime());

        MerchantRecommendation recommendation = new MerchantRecommendation();
        recommendation.setMerchantId(merchantId);
        recommendation.setDishId(request.getDishId());
        recommendation.setTitle(request.getTitle().trim());
        recommendation.setRecommendReason(trimToNull(request.getRecommendReason()));
        recommendation.setRecommendType(normalizeRecommendType(request.getRecommendType()));
        recommendation.setStartTime(request.getStartTime());
        recommendation.setEndTime(request.getEndTime());
        recommendation.setStatus(resolveStatus(request.getEndTime()));
        recommendation.setIsTop(Boolean.TRUE.equals(request.getIsTop()));
        recommendation.setClickCount(0);
        merchantRecommendationMapper.insert(recommendation);
        return recommendation.getId();
    }

    public PageResult<MerchantRecommendationItemVo> listMyRecommendations(
            String recommendType,
            String status,
            Integer page,
            Integer size
    ) {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
        Long merchantId = resolveCurrentMerchantId();
        expireRecommendations(merchantId);

        String normalizedRecommendType = normalizeOptionalRecommendType(recommendType);
        String normalizedStatus = normalizeOptionalStatus(status);
        int currentPage = page == null ? 1 : page;
        int currentSize = size == null ? 10 : size;
        long total = merchantRecommendationMapper.countMerchantRecommendations(
                merchantId,
                normalizedRecommendType,
                normalizedStatus
        );
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }

        long offset = (long) (currentPage - 1) * currentSize;
        return PageResult.of(
                merchantRecommendationMapper.selectMerchantRecommendations(
                        merchantId,
                        normalizedRecommendType,
                        normalizedStatus,
                        offset,
                        currentSize
                ),
                total,
                currentPage,
                currentSize
        );
    }

    @Transactional
    public void updateRecommendation(Long recommendationId, MerchantRecommendationUpdateRequest request) {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
        Long merchantId = resolveCurrentMerchantId();
        validateTimeRange(request.getStartTime(), request.getEndTime());
        MerchantRecommendation recommendation = getOwnedRecommendation(merchantId, recommendationId);
        if (STATUS_CANCELLED.equals(recommendation.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cancelled recommendation cannot be updated");
        }

        merchantRecommendationMapper.update(
                null,
                new LambdaUpdateWrapper<MerchantRecommendation>()
                        .eq(MerchantRecommendation::getId, recommendationId)
                        .set(MerchantRecommendation::getTitle, request.getTitle().trim())
                        .set(MerchantRecommendation::getRecommendReason, trimToNull(request.getRecommendReason()))
                        .set(MerchantRecommendation::getRecommendType, normalizeRecommendType(request.getRecommendType()))
                        .set(MerchantRecommendation::getStartTime, request.getStartTime())
                        .set(MerchantRecommendation::getEndTime, request.getEndTime())
                        .set(MerchantRecommendation::getIsTop, Boolean.TRUE.equals(request.getIsTop()))
                        .set(MerchantRecommendation::getStatus, resolveStatus(request.getEndTime()))
        );
    }

    @Transactional
    public void deleteRecommendation(Long recommendationId) {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
        Long merchantId = resolveCurrentMerchantId();
        getOwnedRecommendation(merchantId, recommendationId);
        merchantRecommendationMapper.update(
                null,
                new LambdaUpdateWrapper<MerchantRecommendation>()
                        .eq(MerchantRecommendation::getId, recommendationId)
                        .set(MerchantRecommendation::getStatus, STATUS_CANCELLED)
        );
    }

    public List<DishRecommendationVo> listDishRecommendations(Long merchantId, String recommendType) {
        expireRecommendations(null);
        if (merchantId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "merchantId must not be null");
        }
        String normalizedRecommendType = normalizeOptionalRecommendType(recommendType);
        List<DishRecommendationVo> records = merchantRecommendationMapper.selectPublicDishRecommendations(merchantId, normalizedRecommendType);
        decorateRecommendationImages(records);
        return records;
    }

    private void decorateRecommendationImages(List<DishRecommendationVo> records) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> dishIds = records.stream().map(DishRecommendationVo::getDishId).distinct().toList();
        Map<Long, List<String>> imageMap = buildImageMap(dishIds);
        for (DishRecommendationVo record : records) {
            List<String> images = imageMap.get(record.getDishId());
            if (images == null || images.isEmpty()) {
                record.setImages(StringUtils.hasText(record.getCoverImageUrl())
                        ? List.of(record.getCoverImageUrl())
                        : Collections.emptyList());
                continue;
            }
            record.setImages(images);
        }
    }

    private Map<Long, List<String>> buildImageMap(List<Long> dishIds) {
        List<DishImageRelationVo> relations = dishMapper.selectDishImagesByDishIds(dishIds);
        Map<Long, List<String>> imageMap = new LinkedHashMap<>();
        for (DishImageRelationVo relation : relations) {
            imageMap.computeIfAbsent(relation.getDishId(), key -> new ArrayList<>()).add(relation.getImageUrl());
        }
        return imageMap;
    }

    private void expireRecommendations(Long merchantId) {
        LambdaUpdateWrapper<MerchantRecommendation> wrapper = new LambdaUpdateWrapper<MerchantRecommendation>()
                .eq(MerchantRecommendation::getStatus, STATUS_ACTIVE)
                .lt(MerchantRecommendation::getEndTime, LocalDateTime.now())
                .set(MerchantRecommendation::getStatus, STATUS_EXPIRED);
        if (merchantId != null) {
            wrapper.eq(MerchantRecommendation::getMerchantId, merchantId);
        }
        merchantRecommendationMapper.update(null, wrapper);
    }

    private MerchantRecommendation getOwnedRecommendation(Long merchantId, Long recommendationId) {
        MerchantRecommendation recommendation = merchantRecommendationMapper.selectOne(
                new LambdaQueryWrapper<MerchantRecommendation>()
                        .eq(MerchantRecommendation::getId, recommendationId)
                        .eq(MerchantRecommendation::getMerchantId, merchantId)
                        .last("LIMIT 1")
        );
        if (recommendation == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Recommendation not found");
        }
        return recommendation;
    }

    private Long resolveCurrentMerchantId() {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, SecurityUtils.getCurrentUserId())
                        .last("LIMIT 1")
        );
        if (merchant == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Merchant not found");
        }
        return merchant.getId();
    }

    private void validateDishOwnership(Long merchantId, Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || !merchantId.equals(dish.getMerchantId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dish not found");
        }
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "startTime and endTime must not be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "endTime must be later than startTime");
        }
    }

    private String normalizeOptionalRecommendType(String recommendType) {
        if (!StringUtils.hasText(recommendType)) {
            return null;
        }
        return normalizeRecommendType(recommendType);
    }

    private String normalizeRecommendType(String recommendType) {
        String normalized = recommendType.trim().toUpperCase();
        if (!RECOMMEND_TYPES.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "recommendType is invalid");
        }
        return normalized;
    }

    private String normalizeOptionalStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (!RECOMMEND_STATUSES.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "status is invalid");
        }
        return normalized;
    }

    private String resolveStatus(LocalDateTime endTime) {
        return endTime.isBefore(LocalDateTime.now()) ? STATUS_EXPIRED : STATUS_ACTIVE;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
