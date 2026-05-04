package com.eatnow.backend.feedback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.dish.entity.Dish;
import com.eatnow.backend.dish.mapper.DishMapper;
import com.eatnow.backend.feedback.dto.MerchantImprovementCreateRequest;
import com.eatnow.backend.feedback.entity.Feedback;
import com.eatnow.backend.feedback.entity.MerchantImprovementRecord;
import com.eatnow.backend.feedback.mapper.FeedbackMapper;
import com.eatnow.backend.feedback.mapper.MerchantImprovementRecordMapper;
import com.eatnow.backend.feedback.vo.MerchantImprovementItemVo;
import com.eatnow.backend.merchant.entity.Merchant;
import com.eatnow.backend.merchant.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MerchantImprovementService {

    private static final String FEEDBACK_STATUS_IMPROVED = "IMPROVED";
    private static final String RECORD_STATUS_PUBLISHED = "PUBLISHED";
    private static final Set<String> RECORD_STATUSES = Set.of("DRAFT", "PUBLISHED", "ARCHIVED");

    private final MerchantImprovementRecordMapper merchantImprovementRecordMapper;
    private final FeedbackMapper feedbackMapper;
    private final MerchantMapper merchantMapper;
    private final DishMapper dishMapper;

    @Transactional
    public Long createImprovementRecord(MerchantImprovementCreateRequest request) {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
        Long merchantId = resolveCurrentMerchantId();
        if (request.getDishId() == null && request.getFeedbackId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "dishId or feedbackId must be provided");
        }

        Feedback feedback = null;
        if (request.getFeedbackId() != null) {
            feedback = feedbackMapper.selectById(request.getFeedbackId());
            if (feedback == null || !merchantId.equals(feedback.getMerchantId())) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "Feedback not found");
            }
        }

        Long finalDishId = request.getDishId();
        if (feedback != null) {
            if (finalDishId == null) {
                finalDishId = feedback.getDishId();
            } else if (!finalDishId.equals(feedback.getDishId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "dishId does not match feedback");
            }
        }
        if (finalDishId != null) {
            validateMerchantDishOwnership(merchantId, finalDishId);
        }

        MerchantImprovementRecord record = new MerchantImprovementRecord();
        record.setMerchantId(merchantId);
        record.setDishId(finalDishId);
        record.setFeedbackId(request.getFeedbackId());
        record.setTitle(request.getTitle().trim());
        record.setContent(request.getContent().trim());
        record.setBeforeDescription(trimToNull(request.getBeforeDescription()));
        record.setAfterDescription(trimToNull(request.getAfterDescription()));
        record.setStatus(normalizeRecordStatus(request.getStatus()));
        record.setIsPublic(request.getIsPublic() == null ? Boolean.TRUE : request.getIsPublic());
        merchantImprovementRecordMapper.insert(record);

        if (feedback != null) {
            feedbackMapper.update(
                    null,
                    new LambdaUpdateWrapper<Feedback>()
                            .eq(Feedback::getId, feedback.getId())
                            .set(Feedback::getStatus, FEEDBACK_STATUS_IMPROVED)
            );
        }
        return record.getId();
    }

    public PageResult<MerchantImprovementItemVo> listImprovementRecords(
            Long dishId,
            Long feedbackId,
            String status,
            Integer page,
            Integer size
    ) {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
        Long merchantId = resolveCurrentMerchantId();
        if (dishId != null) {
            validateMerchantDishOwnership(merchantId, dishId);
        }
        if (feedbackId != null) {
            Feedback feedback = feedbackMapper.selectById(feedbackId);
            if (feedback == null || !merchantId.equals(feedback.getMerchantId())) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "Feedback not found");
            }
        }

        String normalizedStatus = normalizeOptionalRecordStatus(status);
        int currentPage = page == null ? 1 : page;
        int currentSize = size == null ? 10 : size;
        long total = merchantImprovementRecordMapper.countMerchantImprovementRecords(merchantId, dishId, feedbackId, normalizedStatus);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }

        long offset = (long) (currentPage - 1) * currentSize;
        return PageResult.of(
                merchantImprovementRecordMapper.selectMerchantImprovementRecords(merchantId, dishId, feedbackId, normalizedStatus, offset, currentSize),
                total,
                currentPage,
                currentSize
        );
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

    private String normalizeRecordStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return RECORD_STATUS_PUBLISHED;
        }
        String normalized = status.trim().toUpperCase();
        if (!RECORD_STATUSES.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "status is invalid");
        }
        return normalized;
    }

    private String normalizeOptionalRecordStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return normalizeRecordStatus(status);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
