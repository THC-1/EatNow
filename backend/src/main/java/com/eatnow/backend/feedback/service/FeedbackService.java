package com.eatnow.backend.feedback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.dish.entity.Dish;
import com.eatnow.backend.dish.mapper.DishMapper;
import com.eatnow.backend.feedback.dto.FeedbackCreateRequest;
import com.eatnow.backend.feedback.dto.FeedbackReplyRequest;
import com.eatnow.backend.feedback.dto.FeedbackStatusUpdateRequest;
import com.eatnow.backend.feedback.entity.Feedback;
import com.eatnow.backend.feedback.mapper.FeedbackMapper;
import com.eatnow.backend.feedback.vo.FeedbackItemVo;
import com.eatnow.backend.feedback.vo.FeedbackTargetRelationVo;
import com.eatnow.backend.merchant.entity.Merchant;
import com.eatnow.backend.merchant.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private static final String TARGET_TYPE_DISH = "DISH";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_VIEWED = "VIEWED";
    private static final Set<String> FEEDBACK_TYPES = Set.of("TASTE", "PORTION", "PRICE", "SERVICE", "HYGIENE", "OTHER");
    private static final Set<String> FEEDBACK_STATUSES = Set.of("PENDING", "VIEWED", "ACCEPTED", "IMPROVED", "REJECTED");

    private final FeedbackMapper feedbackMapper;
    private final MerchantMapper merchantMapper;
    private final DishMapper dishMapper;

    @Transactional
    public Long createFeedback(FeedbackCreateRequest request) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        Long dishId = normalizeDishTarget(request.getTargetType(), request.getTargetId());
        FeedbackTargetRelationVo target = feedbackMapper.selectVisibleDishTarget(dishId);
        if (target == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dish not found or unavailable for feedback");
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setMerchantId(target.getMerchantId());
        feedback.setDishId(target.getDishId());
        feedback.setTargetType(TARGET_TYPE_DISH);
        feedback.setFeedbackType(normalizeFeedbackType(request.getFeedbackType()));
        feedback.setContent(request.getContent().trim());
        feedback.setStatus(STATUS_PENDING);
        feedbackMapper.insert(feedback);
        return feedback.getId();
    }

    public PageResult<FeedbackItemVo> listMerchantFeedback(
            Long dishId,
            String feedbackType,
            String status,
            Integer page,
            Integer size
    ) {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
        Long merchantId = resolveCurrentMerchantId();
        if (dishId != null) {
            validateMerchantDishOwnership(merchantId, dishId);
        }
        String normalizedFeedbackType = normalizeOptionalFeedbackType(feedbackType);
        String normalizedStatus = normalizeOptionalFeedbackStatus(status);
        int currentPage = page == null ? 1 : page;
        int currentSize = size == null ? 10 : size;

        long total = feedbackMapper.countMerchantFeedback(merchantId, dishId, normalizedFeedbackType, normalizedStatus);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }

        long offset = (long) (currentPage - 1) * currentSize;
        return PageResult.of(
                feedbackMapper.selectMerchantFeedbackList(merchantId, dishId, normalizedFeedbackType, normalizedStatus, offset, currentSize),
                total,
                currentPage,
                currentSize
        );
    }

    @Transactional
    public void replyFeedback(Long feedbackId, FeedbackReplyRequest request) {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
        Long merchantId = resolveCurrentMerchantId();
        Feedback feedback = getMerchantFeedback(feedbackId, merchantId);
        String nextStatus = STATUS_PENDING.equals(feedback.getStatus()) ? STATUS_VIEWED : feedback.getStatus();

        feedbackMapper.update(
                null,
                new LambdaUpdateWrapper<Feedback>()
                        .eq(Feedback::getId, feedbackId)
                        .set(Feedback::getReplyContent, request.getReplyContent().trim())
                        .set(Feedback::getRepliedBy, SecurityUtils.getCurrentUserId())
                        .set(Feedback::getRepliedAt, LocalDateTime.now())
                        .set(Feedback::getStatus, nextStatus)
        );
    }

    @Transactional
    public void updateFeedbackStatus(Long feedbackId, FeedbackStatusUpdateRequest request) {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
        Long merchantId = resolveCurrentMerchantId();
        getMerchantFeedback(feedbackId, merchantId);
        feedbackMapper.update(
                null,
                new LambdaUpdateWrapper<Feedback>()
                        .eq(Feedback::getId, feedbackId)
                        .set(Feedback::getStatus, normalizeFeedbackStatus(request.getStatus()))
        );
    }

    private Feedback getMerchantFeedback(Long feedbackId, Long merchantId) {
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null || !merchantId.equals(feedback.getMerchantId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Feedback not found");
        }
        return feedback;
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

    private void validateMerchantDishOwnership(Long merchantId, Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || !merchantId.equals(dish.getMerchantId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dish not found");
        }
    }

    private Long normalizeDishTarget(String targetType, Long targetId) {
        if (!StringUtils.hasText(targetType) || !TARGET_TYPE_DISH.equals(targetType.trim().toUpperCase())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only DISH feedback is supported");
        }
        if (targetId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "targetId must not be null");
        }
        return targetId;
    }

    private String normalizeOptionalFeedbackType(String feedbackType) {
        if (!StringUtils.hasText(feedbackType)) {
            return null;
        }
        return normalizeFeedbackType(feedbackType);
    }

    private String normalizeFeedbackType(String feedbackType) {
        String normalized = feedbackType.trim().toUpperCase();
        if (!FEEDBACK_TYPES.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "feedbackType is invalid");
        }
        return normalized;
    }

    private String normalizeOptionalFeedbackStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return normalizeFeedbackStatus(status);
    }

    private String normalizeFeedbackStatus(String status) {
        String normalized = status.trim().toUpperCase();
        if (!FEEDBACK_STATUSES.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "status is invalid");
        }
        return normalized;
    }
}
