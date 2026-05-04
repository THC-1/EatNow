package com.eatnow.backend.dish.service;

import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.dish.dto.DishQueryRequest;
import com.eatnow.backend.dish.mapper.CategoryMapper;
import com.eatnow.backend.dish.mapper.DishMapper;
import com.eatnow.backend.dish.mapper.TagMapper;
import com.eatnow.backend.dish.vo.CategoryVo;
import com.eatnow.backend.dish.vo.DishDetailVo;
import com.eatnow.backend.dish.vo.DishImageRelationVo;
import com.eatnow.backend.dish.vo.DishListVo;
import com.eatnow.backend.dish.vo.DishTagRelationVo;
import com.eatnow.backend.dish.vo.DishTagVo;
import com.eatnow.backend.dish.vo.TagVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishQueryService {

    private static final List<String> DEFAULT_VISIBLE_DISH_STATUS = List.of("ON_SALE", "SOLD_OUT");

    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    public PageResult<DishListVo> listDishes(DishQueryRequest query) {
        DishQueryRequest normalizedQuery = normalizeQuery(query);
        long total = dishMapper.countDishes(normalizedQuery);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, normalizedQuery.getPage(), normalizedQuery.getSize());
        }

        long offset = (long) (normalizedQuery.getPage() - 1) * normalizedQuery.getSize();
        List<DishListVo> records = dishMapper.selectDishList(normalizedQuery, offset, normalizedQuery.getSize());
        decorateDishList(records);
        return PageResult.of(records, total, normalizedQuery.getPage(), normalizedQuery.getSize());
    }

    public PageResult<DishListVo> searchDishes(String keyword, Integer page, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "keyword must not be blank");
        }
        DishQueryRequest query = new DishQueryRequest();
        query.setKeyword(keyword);
        query.setPage(page == null ? 1 : page);
        query.setSize(size == null ? 10 : size);
        return listDishes(query);
    }

    public DishDetailVo getDishDetail(Long id) {
        DishDetailVo detail = dishMapper.selectDishDetail(id, DEFAULT_VISIBLE_DISH_STATUS);
        if (detail == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dish not found");
        }

        dishMapper.increaseViewCount(id);
        detail.setViewCount(detail.getViewCount() == null ? 1 : detail.getViewCount() + 1);

        Map<Long, List<String>> imageMap = buildImageMap(List.of(id));
        Map<Long, List<DishTagVo>> tagMap = buildTagMap(List.of(id));
        detail.setImages(imageMap.getOrDefault(id, Collections.emptyList()));
        detail.setTags(tagMap.getOrDefault(id, Collections.emptyList()));
        detail.setRecommendReason(dishMapper.selectCurrentRecommendReason(id));
        return detail;
    }

    public List<CategoryVo> listCategories(String type) {
        return categoryMapper.selectActiveCategories(type);
    }

    public List<TagVo> listTags(String type) {
        return tagMapper.selectActiveTags(type);
    }

    private DishQueryRequest normalizeQuery(DishQueryRequest query) {
        DishQueryRequest normalized = new DishQueryRequest();
        normalized.setCanteenId(query.getCanteenId());
        normalized.setStallId(query.getStallId());
        normalized.setMerchantId(query.getMerchantId());
        normalized.setCategoryId(query.getCategoryId());
        normalized.setTagId(query.getTagId());
        normalized.setMinPrice(query.getMinPrice());
        normalized.setMaxPrice(query.getMaxPrice());
        normalized.setMinScore(query.getMinScore());
        normalized.setKeyword(StringUtils.hasText(query.getKeyword()) ? query.getKeyword().trim() : null);
        normalized.setStatus(StringUtils.hasText(query.getStatus()) ? query.getStatus().trim() : null);
        normalized.setPage(query.getPage() == null ? 1 : query.getPage());
        normalized.setSize(query.getSize() == null ? 10 : query.getSize());
        return normalized;
    }

    private void decorateDishList(List<DishListVo> records) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> dishIds = records.stream().map(DishListVo::getId).toList();
        Map<Long, List<String>> imageMap = buildImageMap(dishIds);
        Map<Long, List<DishTagVo>> tagMap = buildTagMap(dishIds);
        for (DishListVo record : records) {
            record.setImages(imageMap.getOrDefault(record.getId(), Collections.emptyList()));
            record.setTags(tagMap.getOrDefault(record.getId(), Collections.emptyList()));
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

    private Map<Long, List<DishTagVo>> buildTagMap(List<Long> dishIds) {
        List<DishTagRelationVo> relations = dishMapper.selectDishTagsByDishIds(dishIds);
        return relations.stream().collect(Collectors.groupingBy(
                DishTagRelationVo::getDishId,
                LinkedHashMap::new,
                Collectors.mapping(
                        relation -> new DishTagVo(relation.getTagId(), relation.getTagName()),
                        Collectors.toList()
                )
        ));
    }
}
