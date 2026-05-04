package com.eatnow.backend.eatlist.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.eatlist.dto.EatListCreateRequest;
import com.eatnow.backend.eatlist.dto.EatListMarkEatenRequest;
import com.eatnow.backend.eatlist.entity.EatList;
import com.eatnow.backend.eatlist.mapper.EatListMapper;
import com.eatnow.backend.eatlist.vo.EatListDishRelationVo;
import com.eatnow.backend.eatlist.vo.EatListDishSummaryVo;
import com.eatnow.backend.eatlist.vo.EatListItemVo;
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

@Service
@RequiredArgsConstructor
public class EatListService {

    private static final String STATUS_WANT_TO_EAT = "WANT_TO_EAT";
    private static final String STATUS_EATEN = "EATEN";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final EatListMapper eatListMapper;

    @Transactional
    public Long addToEatList(EatListCreateRequest request) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        ensureDishVisible(request.getDishId());

        EatList existing = eatListMapper.selectOne(
                new LambdaQueryWrapper<EatList>()
                        .eq(EatList::getUserId, userId)
                        .eq(EatList::getDishId, request.getDishId())
                        .last("LIMIT 1")
        );
        if (existing != null) {
            eatListMapper.update(
                    null,
                    new LambdaUpdateWrapper<EatList>()
                            .eq(EatList::getId, existing.getId())
                            .set(EatList::getStatus, STATUS_WANT_TO_EAT)
                            .set(EatList::getSourceLotteryRecordId, request.getSourceLotteryRecordId())
                            .set(EatList::getNote, request.getNote())
            );
            return existing.getId();
        }

        EatList eatList = new EatList();
        eatList.setUserId(userId);
        eatList.setDishId(request.getDishId());
        eatList.setStatus(STATUS_WANT_TO_EAT);
        eatList.setSourceLotteryRecordId(request.getSourceLotteryRecordId());
        eatList.setNote(request.getNote());
        eatListMapper.insert(eatList);
        return eatList.getId();
    }

    @Transactional
    public void markAsEaten(Long id, EatListMarkEatenRequest request) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        EatList eatList = getOwnedRecord(id);
        LocalDateTime eatenAt = request.getEatenAt() == null ? LocalDateTime.now() : request.getEatenAt();
        eatListMapper.update(
                null,
                new LambdaUpdateWrapper<EatList>()
                        .eq(EatList::getId, id)
                        .set(EatList::getStatus, STATUS_EATEN)
                        .set(EatList::getEatenAt, eatenAt)
                        .set(EatList::getNote, request.getNote())
        );
    }

    @Transactional
    public void cancel(Long id) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        getOwnedRecord(id);
        eatListMapper.update(
                null,
                new LambdaUpdateWrapper<EatList>()
                        .eq(EatList::getId, id)
                        .set(EatList::getStatus, STATUS_CANCELLED)
        );
    }

    public PageResult<EatListItemVo> list(String status, Integer page, Integer size) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        String normalizedStatus = normalizeStatus(status);
        int currentPage = page == null ? 1 : page;
        int currentSize = size == null ? 10 : size;

        long total = eatListMapper.countEatList(userId, normalizedStatus);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }

        long offset = (long) (currentPage - 1) * currentSize;
        List<EatListItemVo> records = eatListMapper.selectEatList(userId, normalizedStatus, offset, currentSize);
        decorate(records);
        return PageResult.of(records, total, currentPage, currentSize);
    }

    private void decorate(List<EatListItemVo> records) {
        List<Long> dishIds = records.stream().map(EatListItemVo::getDishId).toList();
        Map<Long, EatListDishSummaryVo> dishMap = buildDishMap(dishIds);
        for (EatListItemVo record : records) {
            record.setDish(dishMap.get(record.getDishId()));
        }
    }

    private Map<Long, EatListDishSummaryVo> buildDishMap(List<Long> dishIds) {
        if (dishIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, EatListDishSummaryVo> result = new LinkedHashMap<>();
        for (EatListDishRelationVo relation : eatListMapper.selectDishRelations(dishIds)) {
            EatListDishSummaryVo summaryVo = result.computeIfAbsent(relation.getDishId(), key -> {
                EatListDishSummaryVo vo = new EatListDishSummaryVo();
                vo.setId(relation.getDishId());
                vo.setName(relation.getDishName());
                vo.setPrice(relation.getPrice());
                vo.setScore(relation.getScore());
                vo.setMerchantName(relation.getMerchantName());
                vo.setCanteenName(relation.getCanteenName());
                vo.setImages(new ArrayList<>());
                return vo;
            });
            if (StringUtils.hasText(relation.getImageUrl())) {
                summaryVo.getImages().add(relation.getImageUrl());
            }
        }
        return result;
    }

    private EatList getOwnedRecord(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        EatList eatList = eatListMapper.selectOne(
                new LambdaQueryWrapper<EatList>()
                        .eq(EatList::getId, id)
                        .eq(EatList::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (eatList == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "想吃清单记录不存在");
        }
        return eatList;
    }

    private void ensureDishVisible(Long dishId) {
        Integer count = eatListMapper.countVisibleDishById(dishId);
        if (count == null || count == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "菜品不存在或不可加入清单");
        }
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (!STATUS_WANT_TO_EAT.equals(normalized)
                && !STATUS_EATEN.equals(normalized)
                && !STATUS_CANCELLED.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "eat list status is invalid");
        }
        return normalized;
    }
}
