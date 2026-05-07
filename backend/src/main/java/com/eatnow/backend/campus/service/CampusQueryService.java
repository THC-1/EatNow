package com.eatnow.backend.campus.service;

import com.eatnow.backend.campus.mapper.CanteenMapper;
import com.eatnow.backend.campus.mapper.StallMapper;
import com.eatnow.backend.campus.vo.CampusPlaceSearchVo;
import com.eatnow.backend.campus.vo.CanteenDetailVo;
import com.eatnow.backend.campus.vo.CanteenListVo;
import com.eatnow.backend.campus.vo.StallDetailVo;
import com.eatnow.backend.campus.vo.StallListVo;
import com.eatnow.backend.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampusQueryService {

    private static final String DEFAULT_OPEN_STATUS = "OPEN";
    private static final int DEFAULT_SEARCH_LIMIT = 8;
    private static final int MAX_SEARCH_LIMIT = 20;

    private final CanteenMapper canteenMapper;
    private final StallMapper stallMapper;

    public List<CanteenListVo> listCanteens(Long campusId, String type, String status) {
        return canteenMapper.selectCanteenList(campusId, type, resolveStatus(status));
    }

    public CanteenDetailVo getCanteenDetail(Long id) {
        CanteenDetailVo detail = canteenMapper.selectCanteenDetail(id, DEFAULT_OPEN_STATUS);
        if (detail == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Canteen not found");
        }
        return detail;
    }

    public List<CampusPlaceSearchVo> searchPlaces(String keyword, Integer limit) {
        String normalizedKeyword = trimToNull(keyword);
        if (normalizedKeyword == null) {
            return List.of();
        }
        return canteenMapper.selectPlaceSearchResults(
                normalizedKeyword,
                DEFAULT_OPEN_STATUS,
                normalizeSearchLimit(limit)
        );
    }

    public List<StallListVo> listStalls(Long canteenId, String status) {
        return stallMapper.selectStallList(canteenId, resolveStatus(status));
    }

    public StallDetailVo getStallDetail(Long id) {
        StallDetailVo detail = stallMapper.selectStallDetail(id, DEFAULT_OPEN_STATUS);
        if (detail == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Stall not found");
        }
        return detail;
    }

    private String resolveStatus(String status) {
        return StringUtils.hasText(status) ? status : DEFAULT_OPEN_STATUS;
    }

    private int normalizeSearchLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_SEARCH_LIMIT;
        }
        return Math.min(limit, MAX_SEARCH_LIMIT);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
