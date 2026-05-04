package com.eatnow.backend.merchant.service;

import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.merchant.mapper.MerchantMapper;
import com.eatnow.backend.merchant.vo.MerchantDetailVo;
import com.eatnow.backend.merchant.vo.MerchantListVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantQueryService {

    private static final String DEFAULT_OPEN_STATUS = "OPEN";

    private final MerchantMapper merchantMapper;

    public List<MerchantListVo> listMerchants(Long canteenId, Long stallId, String status) {
        return merchantMapper.selectMerchantList(canteenId, stallId, resolveStatus(status));
    }

    public MerchantDetailVo getMerchantDetail(Long id) {
        MerchantDetailVo detail = merchantMapper.selectMerchantDetail(id, DEFAULT_OPEN_STATUS);
        if (detail == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Merchant not found");
        }
        return detail;
    }

    private String resolveStatus(String status) {
        return StringUtils.hasText(status) ? status : DEFAULT_OPEN_STATUS;
    }
}
