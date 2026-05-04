package com.eatnow.backend.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.merchant.entity.Merchant;
import com.eatnow.backend.merchant.mapper.MerchantMapper;
import com.eatnow.backend.merchant.vo.MerchantFeedbackDishVo;
import com.eatnow.backend.merchant.vo.MerchantPopularDishVo;
import com.eatnow.backend.merchant.vo.MerchantStatisticsOverviewVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantStatisticsService {

    private static final String APPLY_APPROVED = "APPROVED";
    private static final String STATUS_DISABLED = "DISABLED";

    private final MerchantMapper merchantMapper;

    public MerchantStatisticsOverviewVo getOverview() {
        return merchantMapper.selectMerchantStatisticsOverview(getCurrentMerchantId());
    }

    public List<MerchantPopularDishVo> listPopularDishes(Integer limit) {
        return merchantMapper.selectMerchantPopularDishes(getCurrentMerchantId(), normalizeLimit(limit));
    }

    public List<MerchantFeedbackDishVo> listMostFeedbackDishes(Integer limit) {
        return merchantMapper.selectMerchantMostFeedbackDishes(getCurrentMerchantId(), normalizeLimit(limit));
    }

    private Long getCurrentMerchantId() {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, SecurityUtils.getCurrentUserId())
                        .last("LIMIT 1")
        );
        if (merchant == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Merchant not found");
        }
        if (!APPLY_APPROVED.equals(merchant.getApplyStatus()) || STATUS_DISABLED.equals(merchant.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Merchant is not approved or is disabled");
        }
        return merchant.getId();
    }

    private int normalizeLimit(Integer limit) {
        return limit == null ? 10 : Math.min(limit, 50);
    }
}
