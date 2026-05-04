package com.eatnow.backend.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.admin.mapper.AdminMapper;
import com.eatnow.backend.campus.entity.Canteen;
import com.eatnow.backend.campus.entity.Stall;
import com.eatnow.backend.campus.mapper.CanteenMapper;
import com.eatnow.backend.campus.mapper.StallMapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.merchant.dto.MerchantApplyRequest;
import com.eatnow.backend.merchant.dto.MerchantUpdateRequest;
import com.eatnow.backend.merchant.entity.Merchant;
import com.eatnow.backend.merchant.mapper.MerchantMapper;
import com.eatnow.backend.merchant.vo.MerchantApplyVo;
import com.eatnow.backend.merchant.vo.MerchantProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MerchantManageService {

    private static final String APPLY_PENDING = "PENDING";
    private static final String APPLY_APPROVED = "APPROVED";
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_PENDING = "PENDING";
    private static final String CANTEEN_TYPE_CANTEEN = "CANTEEN";
    private static final String CANTEEN_TYPE_CAMPUS_SHOP = "CAMPUS_SHOP";
    private static final String CANTEEN_TYPE_PERIPHERY_SHOP = "PERIPHERY_SHOP";
    private static final long DEFAULT_CAMPUS_ID = 1L;

    private final AdminMapper adminMapper;
    private final MerchantMapper merchantMapper;
    private final StallMapper stallMapper;
    private final CanteenMapper canteenMapper;

    @Transactional
    public MerchantApplyVo applyMerchant(MerchantApplyRequest request) {
        String merchantName = requireText(request.getMerchantName(), "merchantName");
        String storeType = resolveStoreType(request);
        Stall stall = CANTEEN_TYPE_CANTEEN.equals(storeType)
                ? getAvailableCanteenStall(request.getStallId())
                : getOrCreateIndependentStoreStall(request, storeType, merchantName);
        Long userId = SecurityUtils.getCurrentUserId();
        Merchant existing = getMerchantByUserId(userId);
        if (existing == null) {
            ensureStallNotOccupied(stall.getId(), null);
            Merchant merchant = new Merchant();
            merchant.setUserId(userId);
            merchant.setStallId(stall.getId());
            merchant.setName(merchantName);
            merchant.setDescription(trimToNull(request.getDescription()));
            merchant.setContactPhone(trimToNull(request.getContactPhone()));
            merchant.setBusinessHours(trimToNull(request.getBusinessHours()));
            merchant.setApplyStatus(APPLY_PENDING);
            merchant.setStatus(STATUS_PENDING);
            merchantMapper.insert(merchant);
            return new MerchantApplyVo(merchant.getId(), APPLY_PENDING);
        }

        if (APPLY_APPROVED.equals(existing.getApplyStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Merchant application already approved");
        }

        ensureStallNotOccupied(stall.getId(), existing.getId());
        merchantMapper.update(
                null,
                new LambdaUpdateWrapper<Merchant>()
                        .eq(Merchant::getId, existing.getId())
                        .set(Merchant::getStallId, stall.getId())
                        .set(Merchant::getName, merchantName)
                        .set(Merchant::getDescription, trimToNull(request.getDescription()))
                        .set(Merchant::getContactPhone, trimToNull(request.getContactPhone()))
                        .set(Merchant::getBusinessHours, trimToNull(request.getBusinessHours()))
                        .set(Merchant::getApplyStatus, APPLY_PENDING)
                        .set(Merchant::getStatus, STATUS_PENDING)
                        .set(Merchant::getRejectReason, null)
                        .set(Merchant::getApprovedBy, null)
                        .set(Merchant::getApprovedAt, null)
        );
        return new MerchantApplyVo(existing.getId(), APPLY_PENDING);
    }

    public MerchantProfileVo getCurrentMerchantProfile() {
        MerchantProfileVo profile = merchantMapper.selectMerchantProfileByUserId(SecurityUtils.getCurrentUserId());
        if (profile == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Merchant not found");
        }
        return profile;
    }

    @Transactional
    public MerchantProfileVo updateCurrentMerchant(MerchantUpdateRequest request) {
        requireMerchant();
        validateUpdateRequest(request);

        Merchant merchant = getMerchantByUserId(SecurityUtils.getCurrentUserId());
        if (merchant == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Merchant not found");
        }

        LambdaUpdateWrapper<Merchant> updateWrapper = new LambdaUpdateWrapper<Merchant>()
                .eq(Merchant::getId, merchant.getId());

        if (request.getName() != null) {
            updateWrapper.set(Merchant::getName, requireText(request.getName(), "name"));
        }
        if (request.getDescription() != null) {
            updateWrapper.set(Merchant::getDescription, trimToNull(request.getDescription()));
        }
        if (request.getLogoUrl() != null) {
            updateWrapper.set(Merchant::getLogoUrl, trimToNull(request.getLogoUrl()));
        }
        if (request.getBusinessHours() != null) {
            updateWrapper.set(Merchant::getBusinessHours, trimToNull(request.getBusinessHours()));
        }
        if (request.getContactPhone() != null) {
            updateWrapper.set(Merchant::getContactPhone, trimToNull(request.getContactPhone()));
        }

        merchantMapper.update(null, updateWrapper);
        return getCurrentMerchantProfile();
    }

    private Merchant getMerchantByUserId(Long userId) {
        return merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, userId)
                        .last("LIMIT 1")
        );
    }

    private Stall getAvailableCanteenStall(Long stallId) {
        if (stallId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "stallId must not be null for canteen stall application");
        }
        Stall stall = stallMapper.selectById(stallId);
        if (stall == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Stall not found");
        }
        if (!STATUS_OPEN.equals(stall.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Stall is not open");
        }

        Canteen canteen = canteenMapper.selectById(stall.getCanteenId());
        if (canteen == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Canteen not found");
        }
        if (!STATUS_OPEN.equals(canteen.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Canteen is not open");
        }
        if (!CANTEEN_TYPE_CANTEEN.equals(canteen.getType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Selected stall does not belong to a canteen");
        }
        return stall;
    }

    private Stall getOrCreateIndependentStoreStall(MerchantApplyRequest request, String storeType, String merchantName) {
        Long campusId = request.getCampusId() == null ? DEFAULT_CAMPUS_ID : request.getCampusId();
        ensureCampusExists(campusId);

        Canteen store = canteenMapper.selectOne(
                new LambdaQueryWrapper<Canteen>()
                        .eq(Canteen::getCampusId, campusId)
                        .eq(Canteen::getName, merchantName)
                        .last("LIMIT 1")
        );
        if (store == null) {
            store = createIndependentStore(request, campusId, storeType, merchantName);
        } else {
            ensureIndependentStoreUsable(store, storeType);
        }

        Stall stall = stallMapper.selectOne(
                new LambdaQueryWrapper<Stall>()
                        .eq(Stall::getCanteenId, store.getId())
                        .eq(Stall::getName, merchantName)
                        .last("LIMIT 1")
        );
        if (stall == null) {
            stall = createInternalStoreStall(request, store.getId(), merchantName);
        } else if (!STATUS_OPEN.equals(stall.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Independent store is not open");
        }
        return stall;
    }

    private Canteen createIndependentStore(MerchantApplyRequest request, Long campusId, String storeType, String merchantName) {
        Canteen store = new Canteen();
        store.setCampusId(campusId);
        store.setName(merchantName);
        store.setType(storeType);
        store.setDescription(trimToNull(request.getDescription()));
        store.setOpeningHours(trimToNull(request.getBusinessHours()));
        store.setContactPhone(trimToNull(request.getContactPhone()));
        store.setStatus(STATUS_OPEN);
        store.setSortOrder(0);
        canteenMapper.insert(store);
        return store;
    }

    private Stall createInternalStoreStall(MerchantApplyRequest request, Long storeId, String merchantName) {
        Stall stall = new Stall();
        stall.setCanteenId(storeId);
        stall.setName(merchantName);
        stall.setDescription(trimToNull(request.getDescription()));
        stall.setStatus(STATUS_OPEN);
        stall.setSortOrder(0);
        stallMapper.insert(stall);
        return stall;
    }

    private void ensureIndependentStoreUsable(Canteen store, String storeType) {
        if (!storeType.equals(store.getType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Independent store name already exists with another type");
        }
        if (!STATUS_OPEN.equals(store.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Independent store is not open");
        }
    }

    private void ensureStallNotOccupied(Long stallId, Long currentMerchantId) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getStallId, stallId)
                        .last("LIMIT 1")
        );
        if (merchant != null && !merchant.getId().equals(currentMerchantId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Stall already bound to another merchant");
        }
    }

    private void ensureCampusExists(Long campusId) {
        if (adminMapper.countCampusById(campusId) == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Campus not found");
        }
    }

    private String resolveStoreType(MerchantApplyRequest request) {
        if (!StringUtils.hasText(request.getStoreType())) {
            if (request.getStallId() != null) {
                return CANTEEN_TYPE_CANTEEN;
            }
            throw new BusinessException(HttpStatus.BAD_REQUEST, "storeType must not be blank");
        }
        String normalized = request.getStoreType().trim().toUpperCase();
        if (!CANTEEN_TYPE_CANTEEN.equals(normalized)
                && !CANTEEN_TYPE_CAMPUS_SHOP.equals(normalized)
                && !CANTEEN_TYPE_PERIPHERY_SHOP.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "storeType is invalid");
        }
        return normalized;
    }

    private void validateUpdateRequest(MerchantUpdateRequest request) {
        if (request.getName() == null
                && request.getDescription() == null
                && request.getLogoUrl() == null
                && request.getBusinessHours() == null
                && request.getContactPhone() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Update content must not be empty");
        }
    }

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void requireMerchant() {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
    }
}
