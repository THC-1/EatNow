package com.eatnow.backend.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.favorite.service.FavoriteService;
import com.eatnow.backend.lottery.dto.LotteryConditionDrawRequest;
import com.eatnow.backend.lottery.dto.LotteryDrawRequest;
import com.eatnow.backend.lottery.dto.LotteryRecordActionRequest;
import com.eatnow.backend.lottery.entity.LotteryCandidate;
import com.eatnow.backend.lottery.entity.LotteryRecord;
import com.eatnow.backend.lottery.entity.LotteryRule;
import com.eatnow.backend.lottery.mapper.LotteryCandidateMapper;
import com.eatnow.backend.lottery.mapper.LotteryPoolMapper;
import com.eatnow.backend.lottery.mapper.LotteryRecordMapper;
import com.eatnow.backend.lottery.mapper.LotteryRuleMapper;
import com.eatnow.backend.lottery.vo.LotteryDrawResultVo;
import com.eatnow.backend.lottery.vo.LotteryImageRelationVo;
import com.eatnow.backend.lottery.vo.LotteryPoolItemVo;
import com.eatnow.backend.lottery.vo.LotteryRecordVo;
import com.eatnow.backend.lottery.vo.LotteryTagRelationVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LotteryService {

    private static final String DRAW_MODE_RANDOM = "RANDOM";
    private static final String DRAW_MODE_CONDITION = "CONDITION";
    private static final String DRAW_MODE_FAVORITE = "FAVORITE";
    private static final String SOURCE_TYPE_DISH = "DISH";

    private final LotteryRuleMapper lotteryRuleMapper;
    private final LotteryPoolMapper lotteryPoolMapper;
    private final LotteryRecordMapper lotteryRecordMapper;
    private final LotteryCandidateMapper lotteryCandidateMapper;
    private final FavoriteService favoriteService;
    private final ObjectMapper objectMapper;

    private final Random random = new Random();

    @Transactional
    public LotteryDrawResultVo randomDraw(LotteryDrawRequest request) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        String drawMode = normalizeDrawMode(request.getDrawMode(), DRAW_MODE_RANDOM);
        return doDraw(drawMode, null, null, null, null, null, null);
    }

    @Transactional
    public LotteryDrawResultVo conditionDraw(LotteryConditionDrawRequest request) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        String drawMode = normalizeDrawMode(request.getDrawMode(), DRAW_MODE_CONDITION);
        return doDraw(
                drawMode,
                request.getCanteenId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getMinScore(),
                request.getTagIds(),
                null
        );
    }

    @Transactional
    public LotteryDrawResultVo favoriteDraw(LotteryDrawRequest request) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        String drawMode = normalizeDrawMode(request.getDrawMode(), DRAW_MODE_FAVORITE);
        return doDraw(drawMode, null, null, null, null, null, SecurityUtils.getCurrentUserId());
    }

    public PageResult<LotteryRecordVo> listRecords(Integer page, Integer size) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        int currentPage = page == null ? 1 : page;
        int currentSize = size == null ? 10 : size;
        long total = lotteryRecordMapper.countByUserId(userId);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }
        long offset = (long) (currentPage - 1) * currentSize;
        return PageResult.of(lotteryRecordMapper.selectByUserId(userId, offset, currentSize), total, currentPage, currentSize);
    }

    @Transactional
    public void recordAction(Long recordId, LotteryRecordActionRequest request) {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        Long userId = SecurityUtils.getCurrentUserId();
        LotteryRecord record = lotteryRecordMapper.selectOne(
                new LambdaQueryWrapper<LotteryRecord>()
                        .eq(LotteryRecord::getId, recordId)
                        .eq(LotteryRecord::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (record == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "抽奖记录不存在");
        }
        String resultAction = normalizeResultAction(request.getResultAction());
        lotteryRecordMapper.update(
                null,
                new LambdaUpdateWrapper<LotteryRecord>()
                        .eq(LotteryRecord::getId, recordId)
                        .set(LotteryRecord::getResultAction, resultAction)
                        .set(LotteryRecord::getActionAt, LocalDateTime.now())
        );

        if ("FAVORITE".equals(resultAction) && SOURCE_TYPE_DISH.equals(record.getSourceType())) {
            favoriteService.createFavoriteIfAbsent(userId, SOURCE_TYPE_DISH, record.getSourceId());
        }
    }

    private LotteryDrawResultVo doDraw(
            String drawMode,
            Long canteenId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minScore,
            List<Long> tagIds,
            Long favoriteUserId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        LotteryRule lotteryRule = lotteryRuleMapper.selectActiveRule(drawMode);
        if (lotteryRule == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "抽奖规则不存在");
        }

        List<LotteryPoolItemVo> eligiblePools = lotteryPoolMapper.selectEligiblePools(
                canteenId,
                minPrice,
                maxPrice,
                minScore,
                tagIds,
                favoriteUserId
        );
        if (eligiblePools.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "没有符合条件的抽奖候选菜品");
        }

        List<LotteryPoolItemVo> candidates = pickCandidates(eligiblePools, Math.min(3, eligiblePools.size()));
        LotteryPoolItemVo selected = pickOne(candidates);

        LotteryRecord record = new LotteryRecord();
        record.setUserId(userId);
        record.setRuleId(lotteryRule.getId());
        record.setDrawMode(drawMode);
        record.setSourceType(selected.getSourceType());
        record.setSourceId(selected.getSourceId());
        record.setSnapshotTitle(selected.getTitle());
        record.setSnapshotPrice(selected.getPrice());
        record.setSnapshotScore(selected.getScore());
        record.setConditionJson(buildConditionJson(canteenId, minPrice, maxPrice, minScore, tagIds));
        record.setResultAction("NONE");
        lotteryRecordMapper.insert(record);

        for (int i = 0; i < candidates.size(); i++) {
            LotteryPoolItemVo candidate = candidates.get(i);
            LotteryCandidate lotteryCandidate = new LotteryCandidate();
            lotteryCandidate.setRecordId(record.getId());
            lotteryCandidate.setSourceType(candidate.getSourceType());
            lotteryCandidate.setSourceId(candidate.getSourceId());
            lotteryCandidate.setTitle(candidate.getTitle());
            lotteryCandidate.setPrice(candidate.getPrice());
            lotteryCandidate.setScore(candidate.getScore());
            lotteryCandidate.setSortOrder(i);
            lotteryCandidate.setIsSelected(candidate.getSourceId().equals(selected.getSourceId()));
            lotteryCandidateMapper.insert(lotteryCandidate);
        }

        return buildResultVo(record.getId(), selected);
    }

    private LotteryDrawResultVo buildResultVo(Long recordId, LotteryPoolItemVo selected) {
        LotteryDrawResultVo resultVo = new LotteryDrawResultVo();
        resultVo.setRecordId(recordId);
        resultVo.setSourceType(selected.getSourceType());
        resultVo.setSourceId(selected.getSourceId());
        resultVo.setTitle(selected.getTitle());
        resultVo.setCanteenName(selected.getCanteenName());
        resultVo.setCanteenType(selected.getCanteenType());
        resultVo.setMerchantName(selected.getMerchantName());
        resultVo.setPrice(selected.getPrice());
        resultVo.setScore(selected.getScore());
        resultVo.setRecommendReason(selected.getRecommendReason());
        resultVo.setTags(buildTagMap(List.of(selected.getSourceId())).getOrDefault(selected.getSourceId(), Collections.emptyList()));
        resultVo.setImages(buildImageMap(List.of(selected.getSourceId())).getOrDefault(selected.getSourceId(), buildFallbackImages(selected)));
        return resultVo;
    }

    private List<String> buildFallbackImages(LotteryPoolItemVo selected) {
        if (!StringUtils.hasText(selected.getCoverImageUrl())) {
            return Collections.emptyList();
        }
        return List.of(selected.getCoverImageUrl());
    }

    private Map<Long, List<String>> buildTagMap(List<Long> dishIds) {
        List<LotteryTagRelationVo> relations = lotteryPoolMapper.selectDishTags(dishIds);
        Map<Long, List<String>> tagMap = new LinkedHashMap<>();
        for (LotteryTagRelationVo relation : relations) {
            tagMap.computeIfAbsent(relation.getSourceId(), key -> new ArrayList<>()).add(relation.getTagName());
        }
        return tagMap;
    }

    private Map<Long, List<String>> buildImageMap(List<Long> dishIds) {
        List<LotteryImageRelationVo> relations = lotteryPoolMapper.selectDishImages(dishIds);
        Map<Long, List<String>> imageMap = new LinkedHashMap<>();
        for (LotteryImageRelationVo relation : relations) {
            imageMap.computeIfAbsent(relation.getSourceId(), key -> new ArrayList<>()).add(relation.getImageUrl());
        }
        return imageMap;
    }

    private List<LotteryPoolItemVo> pickCandidates(List<LotteryPoolItemVo> poolItems, int count) {
        List<LotteryPoolItemVo> available = new ArrayList<>(poolItems);
        List<LotteryPoolItemVo> selected = new ArrayList<>();
        while (!available.isEmpty() && selected.size() < count) {
            LotteryPoolItemVo item = pickOne(available);
            selected.add(item);
            available.removeIf(candidate -> candidate.getSourceId().equals(item.getSourceId()) && candidate.getSourceType().equals(item.getSourceType()));
        }
        return selected;
    }

    private LotteryPoolItemVo pickOne(List<LotteryPoolItemVo> poolItems) {
        int totalWeight = poolItems.stream().mapToInt(item -> item.getWeight() == null || item.getWeight() <= 0 ? 1 : item.getWeight()).sum();
        int hit = random.nextInt(Math.max(totalWeight, 1)) + 1;
        int current = 0;
        for (LotteryPoolItemVo poolItem : poolItems) {
            current += poolItem.getWeight() == null || poolItem.getWeight() <= 0 ? 1 : poolItem.getWeight();
            if (hit <= current) {
                return poolItem;
            }
        }
        return poolItems.get(0);
    }

    private String normalizeDrawMode(String drawMode, String expectedMode) {
        if (!StringUtils.hasText(drawMode)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "drawMode must not be blank");
        }
        String normalized = drawMode.trim().toUpperCase();
        if (!expectedMode.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "drawMode is invalid");
        }
        return normalized;
    }

    private String normalizeResultAction(String resultAction) {
        if (!StringUtils.hasText(resultAction)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "resultAction must not be blank");
        }
        String normalized = resultAction.trim().toUpperCase();
        if (!"NONE".equals(normalized)
                && !"ACCEPT".equals(normalized)
                && !"SKIP".equals(normalized)
                && !"FAVORITE".equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "resultAction is invalid");
        }
        return normalized;
    }

    private String buildConditionJson(
            Long canteenId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minScore,
            List<Long> tagIds
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("canteenId", canteenId);
        payload.put("minPrice", minPrice);
        payload.put("maxPrice", maxPrice);
        payload.put("minScore", minScore);
        payload.put("tagIds", tagIds);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "抽奖条件序列化失败");
        }
    }
}
