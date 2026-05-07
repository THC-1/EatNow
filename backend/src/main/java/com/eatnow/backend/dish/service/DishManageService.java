package com.eatnow.backend.dish.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.campus.entity.Canteen;
import com.eatnow.backend.campus.entity.Stall;
import com.eatnow.backend.campus.mapper.CanteenMapper;
import com.eatnow.backend.campus.mapper.StallMapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.dish.dto.DishSaveRequest;
import com.eatnow.backend.dish.dto.DishStatusUpdateRequest;
import com.eatnow.backend.dish.entity.Category;
import com.eatnow.backend.dish.entity.Dish;
import com.eatnow.backend.dish.entity.Tag;
import com.eatnow.backend.dish.mapper.CategoryMapper;
import com.eatnow.backend.dish.mapper.DishMapper;
import com.eatnow.backend.dish.mapper.TagMapper;
import com.eatnow.backend.dish.vo.DishDetailVo;
import com.eatnow.backend.dish.vo.DishImageRelationVo;
import com.eatnow.backend.dish.vo.DishTagRelationVo;
import com.eatnow.backend.dish.vo.DishTagVo;
import com.eatnow.backend.merchant.entity.Merchant;
import com.eatnow.backend.merchant.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishManageService {

    private static final String APPLY_APPROVED = "APPROVED";
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_DISABLED = "DISABLED";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String DISH_STATUS_PENDING = "PENDING";
    private static final String DISH_STATUS_ON_SALE = "ON_SALE";
    private static final String DISH_STATUS_SOLD_OUT = "SOLD_OUT";
    private static final String DISH_STATUS_OFF_SHELF = "OFF_SHELF";
    private static final String DISH_STATUS_DELETED = "DELETED";
    private static final String LOTTERY_POOL_STATUS_INACTIVE = "INACTIVE";

    private final DishMapper dishMapper;
    private final MerchantMapper merchantMapper;
    private final StallMapper stallMapper;
    private final CanteenMapper canteenMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @Transactional
    public Long createDish(DishSaveRequest request) {
        Merchant merchant = getCurrentApprovedMerchant();
        Stall stall = getOpenStall(merchant.getStallId());
        Canteen canteen = getOpenCanteen(stall.getCanteenId());

        String name = normalizeName(request.getName());
        ensureDishNameAvailable(merchant.getId(), name, null);
        validateCategory(request.getCategoryId());
        List<Long> tagIds = normalizeTagIds(request.getTagIds());
        List<String> images = normalizeImages(request.getImages());

        Dish dish = new Dish();
        dish.setMerchantId(merchant.getId());
        dish.setCanteenId(canteen.getId());
        dish.setStallId(stall.getId());
        dish.setCategoryId(request.getCategoryId());
        dish.setName(name);
        dish.setDescription(trimToNull(request.getDescription()));
        dish.setPrice(request.getPrice());
        dish.setCoverImageUrl(firstImageOrNull(images));
        dish.setStatus(DISH_STATUS_PENDING);
        dish.setIsJoinLottery(request.getIsJoinLottery() == null || request.getIsJoinLottery());
        dishMapper.insert(dish);

        replaceImages(dish.getId(), images);
        replaceTags(dish.getId(), tagIds);
        dishMapper.upsertDishLotteryPool(dish.getId());
        return dish.getId();
    }

    @Transactional
    public DishDetailVo updateDish(Long dishId, DishSaveRequest request) {
        Merchant merchant = getCurrentApprovedMerchant();
        Dish existing = getOwnedDish(dishId, merchant.getId());

        String name = normalizeName(request.getName());
        ensureDishNameAvailable(merchant.getId(), name, dishId);
        validateCategory(request.getCategoryId());
        List<Long> tagIds = normalizeTagIds(request.getTagIds());
        List<String> images = normalizeImages(request.getImages());

        dishMapper.update(
                null,
                new LambdaUpdateWrapper<Dish>()
                        .eq(Dish::getId, existing.getId())
                        .set(Dish::getCategoryId, request.getCategoryId())
                        .set(Dish::getName, name)
                        .set(Dish::getDescription, trimToNull(request.getDescription()))
                        .set(Dish::getPrice, request.getPrice())
                        .set(Dish::getCoverImageUrl, firstImageOrNull(images))
                        .set(Dish::getIsJoinLottery, request.getIsJoinLottery() == null || request.getIsJoinLottery())
        );
        replaceImages(dishId, images);
        replaceTags(dishId, tagIds);
        dishMapper.upsertDishLotteryPool(dishId);
        return getManageDishDetail(dishId, merchant.getId());
    }

    @Transactional
    public void updateDishStatus(Long dishId, DishStatusUpdateRequest request) {
        String normalizedStatus = normalizeDishStatus(request.getStatus());
        boolean isAdmin = SecurityUtils.hasRole(RoleCode.ADMIN);
        Long merchantId = null;
        if (!isAdmin) {
            merchantId = getCurrentApprovedMerchant().getId();
        }

        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dish not found");
        }
        if (merchantId != null && !merchantId.equals(dish.getMerchantId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No permission to manage this dish");
        }

        dishMapper.update(
                null,
                new LambdaUpdateWrapper<Dish>()
                        .eq(Dish::getId, dishId)
                        .set(Dish::getStatus, normalizedStatus)
        );
        syncDishLotteryPool(dishId, normalizedStatus);
    }

    @Transactional
    public void deleteDish(Long dishId) {
        Merchant merchant = getCurrentApprovedMerchant();
        getOwnedDish(dishId, merchant.getId());
        dishMapper.update(
                null,
                new LambdaUpdateWrapper<Dish>()
                        .eq(Dish::getId, dishId)
                        .set(Dish::getStatus, DISH_STATUS_DELETED)
        );
        dishMapper.updateDishLotteryPoolStatus(dishId, LOTTERY_POOL_STATUS_INACTIVE);
    }

    private DishDetailVo getManageDishDetail(Long dishId, Long merchantId) {
        DishDetailVo detail = dishMapper.selectDishManageDetail(dishId, merchantId);
        if (detail == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dish not found");
        }
        Map<Long, List<String>> imageMap = buildImageMap(List.of(dishId));
        Map<Long, List<DishTagVo>> tagMap = buildTagMap(List.of(dishId));
        detail.setImages(imageMap.getOrDefault(dishId, Collections.emptyList()));
        detail.setTags(tagMap.getOrDefault(dishId, Collections.emptyList()));
        return detail;
    }

    private Merchant getCurrentApprovedMerchant() {
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
        return merchant;
    }

    private Stall getOpenStall(Long stallId) {
        Stall stall = stallMapper.selectById(stallId);
        if (stall == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Stall not found");
        }
        if (!STATUS_OPEN.equals(stall.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Stall is not open");
        }
        return stall;
    }

    private Canteen getOpenCanteen(Long canteenId) {
        Canteen canteen = canteenMapper.selectById(canteenId);
        if (canteen == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Canteen not found");
        }
        if (!STATUS_OPEN.equals(canteen.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Canteen is not open");
        }
        return canteen;
    }

    private Dish getOwnedDish(Long dishId, Long merchantId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dish not found");
        }
        if (!merchantId.equals(dish.getMerchantId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No permission to manage this dish");
        }
        if (DISH_STATUS_DELETED.equals(dish.getStatus())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dish not found");
        }
        return dish;
    }

    private void ensureDishNameAvailable(Long merchantId, String name, Long excludingDishId) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getMerchantId, merchantId)
                .eq(Dish::getName, name)
                .ne(Dish::getStatus, DISH_STATUS_DELETED);
        if (excludingDishId != null) {
            wrapper.ne(Dish::getId, excludingDishId);
        }
        if (dishMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Dish name already exists");
        }
    }

    private void validateCategory(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || !STATUS_ACTIVE.equals(category.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Category is invalid");
        }
    }

    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> uniqueIds = tagIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (uniqueIds.size() != tagIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "tagIds is invalid");
        }
        long activeCount = tagMapper.selectCount(
                new LambdaQueryWrapper<Tag>()
                        .in(Tag::getId, uniqueIds)
                        .eq(Tag::getStatus, STATUS_ACTIVE)
        );
        if (activeCount != uniqueIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "tagIds is invalid");
        }
        return new ArrayList<>(uniqueIds);
    }

    private List<String> normalizeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (String image : images) {
            String trimmed = trimToNull(image);
            if (trimmed == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "images must not contain blank url");
            }
            if (trimmed.length() > 255) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "image url length must be less than or equal to 255");
            }
            normalized.add(trimmed);
        }
        return normalized;
    }

    private void replaceImages(Long dishId, List<String> images) {
        dishMapper.deleteDishImages(dishId);
        if (!images.isEmpty()) {
            dishMapper.insertDishImages(dishId, images);
        }
    }

    private void replaceTags(Long dishId, List<Long> tagIds) {
        dishMapper.deleteDishTags(dishId);
        if (!tagIds.isEmpty()) {
            dishMapper.insertDishTags(dishId, tagIds);
        }
    }

    private void syncDishLotteryPool(Long dishId, String dishStatus) {
        if (DISH_STATUS_ON_SALE.equals(dishStatus) || DISH_STATUS_SOLD_OUT.equals(dishStatus)) {
            dishMapper.upsertDishLotteryPool(dishId);
            return;
        }
        dishMapper.updateDishLotteryPoolStatus(dishId, LOTTERY_POOL_STATUS_INACTIVE);
    }

    private String normalizeDishStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "status must not be blank");
        }
        String normalized = status.trim().toUpperCase();
        if (!DISH_STATUS_PENDING.equals(normalized)
                && !DISH_STATUS_ON_SALE.equals(normalized)
                && !DISH_STATUS_SOLD_OUT.equals(normalized)
                && !DISH_STATUS_OFF_SHELF.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "status is invalid");
        }
        return normalized;
    }

    private String normalizeName(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "name must not be blank");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String firstImageOrNull(List<String> images) {
        return images.isEmpty() ? null : images.get(0);
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
