package com.eatnow.backend.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.admin.dto.AdminCanteenCreateRequest;
import com.eatnow.backend.admin.dto.AdminCanteenUpdateRequest;
import com.eatnow.backend.admin.dto.AdminDishStatusUpdateRequest;
import com.eatnow.backend.admin.dto.AdminStallCreateRequest;
import com.eatnow.backend.admin.dto.AdminStallUpdateRequest;
import com.eatnow.backend.admin.dto.MerchantApplicationRejectRequest;
import com.eatnow.backend.admin.mapper.AdminMapper;
import com.eatnow.backend.admin.vo.AdminCanteenScoreVo;
import com.eatnow.backend.admin.vo.AdminDishDetailVo;
import com.eatnow.backend.admin.vo.AdminDishItemVo;
import com.eatnow.backend.admin.vo.AdminMerchantApplicationItemVo;
import com.eatnow.backend.admin.vo.AdminMerchantProfileVo;
import com.eatnow.backend.admin.vo.AdminPopularDishVo;
import com.eatnow.backend.admin.vo.AdminPopularMerchantVo;
import com.eatnow.backend.admin.vo.AdminReviewItemVo;
import com.eatnow.backend.admin.vo.AdminStatisticsOverviewVo;
import com.eatnow.backend.admin.vo.AdminStudentProfileVo;
import com.eatnow.backend.admin.vo.AdminUserActivityVo;
import com.eatnow.backend.admin.vo.AdminUserDetailVo;
import com.eatnow.backend.admin.vo.AdminUserListItemVo;
import com.eatnow.backend.campus.entity.Canteen;
import com.eatnow.backend.campus.entity.Stall;
import com.eatnow.backend.campus.mapper.CanteenMapper;
import com.eatnow.backend.campus.mapper.StallMapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.enums.UserStatus;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.dish.entity.Dish;
import com.eatnow.backend.dish.mapper.DishMapper;
import com.eatnow.backend.dish.vo.DishImageRelationVo;
import com.eatnow.backend.dish.vo.DishTagRelationVo;
import com.eatnow.backend.dish.vo.DishTagVo;
import com.eatnow.backend.merchant.entity.Merchant;
import com.eatnow.backend.merchant.mapper.MerchantMapper;
import com.eatnow.backend.review.entity.Review;
import com.eatnow.backend.review.mapper.ReviewMapper;
import com.eatnow.backend.review.service.ReviewService;
import com.eatnow.backend.review.vo.ReviewImageRelationVo;
import com.eatnow.backend.user.entity.SysUser;
import com.eatnow.backend.user.entity.SysUserRole;
import com.eatnow.backend.user.mapper.SysRoleMapper;
import com.eatnow.backend.user.mapper.SysUserMapper;
import com.eatnow.backend.user.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_MERCHANT = "MERCHANT";

    private static final String APPLY_PENDING = "PENDING";
    private static final String APPLY_APPROVED = "APPROVED";
    private static final String APPLY_REJECTED = "REJECTED";

    private static final String MERCHANT_STATUS_PENDING = "PENDING";
    private static final String MERCHANT_STATUS_OPEN = "OPEN";
    private static final String MERCHANT_STATUS_DISABLED = "DISABLED";

    private static final String CANTEEN_TYPE_CANTEEN = "CANTEEN";
    private static final String CANTEEN_TYPE_CAMPUS_SHOP = "CAMPUS_SHOP";
    private static final String CANTEEN_TYPE_PERIPHERY_SHOP = "PERIPHERY_SHOP";

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String STATUS_DISABLED = "DISABLED";

    private static final String REVIEW_TARGET_DISH = "DISH";
    private static final String REVIEW_STATUS_VISIBLE = "VISIBLE";
    private static final String REVIEW_STATUS_HIDDEN = "HIDDEN";
    private static final String REVIEW_STATUS_DELETED = "DELETED";

    private static final String DISH_STATUS_PENDING = "PENDING";
    private static final String DISH_STATUS_ON_SALE = "ON_SALE";
    private static final String DISH_STATUS_SOLD_OUT = "SOLD_OUT";
    private static final String DISH_STATUS_OFF_SHELF = "OFF_SHELF";

    private static final String LOTTERY_POOL_STATUS_ACTIVE = "ACTIVE";
    private static final String LOTTERY_POOL_STATUS_INACTIVE = "INACTIVE";

    private final AdminMapper adminMapper;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final MerchantMapper merchantMapper;
    private final DishMapper dishMapper;
    private final CanteenMapper canteenMapper;
    private final StallMapper stallMapper;
    private final ReviewMapper reviewMapper;
    private final ReviewService reviewService;

    public PageResult<AdminUserListItemVo> listUsers(String role, String status, String keyword, Integer page, Integer size) {
        requireAdmin();
        String normalizedRole = normalizeUserRole(role);
        String normalizedStatus = normalizeUserStatus(status);
        int currentPage = normalizePage(page);
        int currentSize = normalizeSize(size);
        long total = adminMapper.countAdminUserList(normalizedRole, normalizedStatus, normalizeKeyword(keyword));
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }
        long offset = (long) (currentPage - 1) * currentSize;
        List<AdminUserListItemVo> records = adminMapper.selectAdminUserList(
                normalizedRole,
                normalizedStatus,
                normalizeKeyword(keyword),
                offset,
                currentSize
        );
        return PageResult.of(records, total, currentPage, currentSize);
    }

    public AdminUserDetailVo getUserDetail(Long userId) {
        requireAdmin();
        AdminUserDetailVo detail = adminMapper.selectAdminUserDetail(userId);
        if (detail == null || isAdminUser(userId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        detail.setRoles(sysUserMapper.selectRoleCodesByUserId(userId));
        AdminStudentProfileVo studentProfile = adminMapper.selectStudentProfile(userId);
        AdminMerchantProfileVo merchantProfile = adminMapper.selectMerchantProfile(userId);
        detail.setStudentProfile(studentProfile);
        detail.setMerchantProfile(merchantProfile);
        detail.setReviewCount(Math.toIntExact(reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getUserId, userId)
                        .ne(Review::getStatus, REVIEW_STATUS_DELETED)
        )));
        return detail;
    }

    @Transactional
    public void disableUser(Long userId) {
        requireAdmin();
        SysUser user = getManageableUser(userId);
        if (STATUS_DISABLED.equals(user.getStatus())) {
            return;
        }
        sysUserMapper.update(
                null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, userId)
                        .set(SysUser::getStatus, UserStatus.DISABLED.name())
        );
        syncMerchantStatusOnUserStateChange(userId, true);
    }

    @Transactional
    public void enableUser(Long userId) {
        requireAdmin();
        SysUser user = getManageableUser(userId);
        if (UserStatus.ACTIVE.name().equals(user.getStatus())) {
            return;
        }
        sysUserMapper.update(
                null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, userId)
                        .set(SysUser::getStatus, UserStatus.ACTIVE.name())
        );
        syncMerchantStatusOnUserStateChange(userId, false);
    }

    public PageResult<AdminMerchantApplicationItemVo> listMerchantApplications(String applyStatus, Integer page, Integer size) {
        requireAdmin();
        String normalizedApplyStatus = normalizeApplyStatus(applyStatus);
        int currentPage = normalizePage(page);
        int currentSize = normalizeSize(size);
        long total = adminMapper.countMerchantApplications(normalizedApplyStatus);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }
        long offset = (long) (currentPage - 1) * currentSize;
        List<AdminMerchantApplicationItemVo> records = adminMapper.selectMerchantApplications(
                normalizedApplyStatus,
                offset,
                currentSize
        );
        return PageResult.of(records, total, currentPage, currentSize);
    }

    @Transactional
    public void approveMerchantApplication(Long merchantId) {
        requireAdmin();
        Merchant merchant = getMerchantOrThrow(merchantId);
        if (!APPLY_PENDING.equals(merchant.getApplyStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "当前申请状态不允许审核通过");
        }
        SysUser user = sysUserMapper.selectById(merchant.getUserId());
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商家关联用户不存在");
        }
        if (!UserStatus.ACTIVE.name().equals(user.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "商家用户已被禁用，无法审核通过");
        }
        merchantMapper.update(
                null,
                new LambdaUpdateWrapper<Merchant>()
                        .eq(Merchant::getId, merchantId)
                        .set(Merchant::getApplyStatus, APPLY_APPROVED)
                        .set(Merchant::getStatus, MERCHANT_STATUS_OPEN)
                        .set(Merchant::getRejectReason, null)
                        .set(Merchant::getApprovedBy, SecurityUtils.getCurrentUserId())
                        .set(Merchant::getApprovedAt, LocalDateTime.now())
        );
        ensureUserRole(merchant.getUserId(), RoleCode.MERCHANT);
    }

    @Transactional
    public void rejectMerchantApplication(Long merchantId, MerchantApplicationRejectRequest request) {
        requireAdmin();
        Merchant merchant = getMerchantOrThrow(merchantId);
        if (!APPLY_PENDING.equals(merchant.getApplyStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "当前申请状态不允许驳回");
        }
        merchantMapper.update(
                null,
                new LambdaUpdateWrapper<Merchant>()
                        .eq(Merchant::getId, merchantId)
                        .set(Merchant::getApplyStatus, APPLY_REJECTED)
                        .set(Merchant::getStatus, MERCHANT_STATUS_PENDING)
                        .set(Merchant::getRejectReason, request.getRejectReason().trim())
                        .set(Merchant::getApprovedBy, null)
                        .set(Merchant::getApprovedAt, null)
        );
    }

    @Transactional
    public Long createCanteen(AdminCanteenCreateRequest request) {
        requireAdmin();
        ensureCampusExists(request.getCampusId());

        Canteen canteen = new Canteen();
        canteen.setCampusId(request.getCampusId());
        canteen.setName(requireText(request.getName(), "name"));
        canteen.setType(normalizeCanteenType(request.getType()));
        canteen.setLocation(trimToNull(request.getLocation()));
        canteen.setDescription(trimToNull(request.getDescription()));
        canteen.setOpeningHours(trimToNull(request.getOpeningHours()));
        canteen.setStatus(STATUS_OPEN);
        canteen.setSortOrder(0);
        canteenMapper.insert(canteen);
        return canteen.getId();
    }

    @Transactional
    public void updateCanteen(Long canteenId, AdminCanteenUpdateRequest request) {
        requireAdmin();
        Canteen existing = canteenMapper.selectById(canteenId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "食堂/店铺不存在");
        }
        validateCanteenUpdateRequest(request);
        LambdaUpdateWrapper<Canteen> wrapper = new LambdaUpdateWrapper<Canteen>().eq(Canteen::getId, canteenId);

        if (request.getCampusId() != null) {
            ensureCampusExists(request.getCampusId());
            wrapper.set(Canteen::getCampusId, request.getCampusId());
        }
        if (request.getName() != null) {
            wrapper.set(Canteen::getName, requireText(request.getName(), "name"));
        }
        if (request.getType() != null) {
            wrapper.set(Canteen::getType, normalizeCanteenType(request.getType()));
        }
        if (request.getLocation() != null) {
            wrapper.set(Canteen::getLocation, trimToNull(request.getLocation()));
        }
        if (request.getDescription() != null) {
            wrapper.set(Canteen::getDescription, trimToNull(request.getDescription()));
        }
        if (request.getOpeningHours() != null) {
            wrapper.set(Canteen::getOpeningHours, trimToNull(request.getOpeningHours()));
        }
        if (request.getStatus() != null) {
            wrapper.set(Canteen::getStatus, normalizeBusinessStatus(request.getStatus()));
        }
        canteenMapper.update(null, wrapper);
    }

    @Transactional
    public void deleteCanteen(Long canteenId) {
        requireAdmin();
        if (canteenMapper.selectById(canteenId) == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "食堂/店铺不存在");
        }
        if (adminMapper.countStallsByCanteenId(canteenId) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "该食堂/店铺下仍有关联窗口或内部绑定，无法删除");
        }
        canteenMapper.deleteById(canteenId);
    }

    @Transactional
    public Long createStall(AdminStallCreateRequest request) {
        requireAdmin();
        Canteen canteen = ensureCanteenExists(request.getCanteenId());
        if (!CANTEEN_TYPE_CANTEEN.equals(canteen.getType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only canteens can create windows");
        }

        Stall stall = new Stall();
        stall.setCanteenId(request.getCanteenId());
        stall.setName(requireText(request.getName(), "name"));
        stall.setLocation(trimToNull(request.getLocation()));
        stall.setDescription(trimToNull(request.getDescription()));
        stall.setStatus(STATUS_OPEN);
        stall.setSortOrder(0);
        stallMapper.insert(stall);
        return stall.getId();
    }

    @Transactional
    public void updateStall(Long stallId, AdminStallUpdateRequest request) {
        requireAdmin();
        Stall existing = stallMapper.selectById(stallId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "店铺不存在");
        }
        validateStallUpdateRequest(request);
        LambdaUpdateWrapper<Stall> wrapper = new LambdaUpdateWrapper<Stall>().eq(Stall::getId, stallId);

        if (request.getName() != null) {
            wrapper.set(Stall::getName, requireText(request.getName(), "name"));
        }
        if (request.getLocation() != null) {
            wrapper.set(Stall::getLocation, trimToNull(request.getLocation()));
        }
        if (request.getDescription() != null) {
            wrapper.set(Stall::getDescription, trimToNull(request.getDescription()));
        }
        if (request.getStatus() != null) {
            wrapper.set(Stall::getStatus, normalizeBusinessStatus(request.getStatus()));
        }
        stallMapper.update(null, wrapper);
    }

    @Transactional
    public void deleteStall(Long stallId) {
        requireAdmin();
        if (stallMapper.selectById(stallId) == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "店铺不存在");
        }
        if (adminMapper.countMerchantByStallId(stallId) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "该店铺已绑定商家，无法删除");
        }
        if (adminMapper.countDishByStallId(stallId) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "该店铺下仍有关联菜品，无法删除");
        }
        stallMapper.deleteById(stallId);
    }

    public PageResult<AdminDishItemVo> listDishes(
            Long merchantId,
            Long canteenId,
            Long stallId,
            String status,
            String keyword,
            Integer page,
            Integer size
    ) {
        requireAdmin();
        String normalizedStatus = normalizeDishStatus(status, true);
        int currentPage = normalizePage(page);
        int currentSize = normalizeSize(size);
        String normalizedKeyword = normalizeKeyword(keyword);
        long total = adminMapper.countAdminDishList(merchantId, canteenId, stallId, normalizedStatus, normalizedKeyword);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }
        long offset = (long) (currentPage - 1) * currentSize;
        List<AdminDishItemVo> records = adminMapper.selectAdminDishList(
                merchantId,
                canteenId,
                stallId,
                normalizedStatus,
                normalizedKeyword,
                offset,
                currentSize
        );
        return PageResult.of(records, total, currentPage, currentSize);
    }

    public AdminDishDetailVo getDishDetail(Long dishId) {
        requireAdmin();
        AdminDishDetailVo detail = adminMapper.selectAdminDishDetail(dishId);
        if (detail == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dish not found");
        }
        decorateDishDetail(detail);
        return detail;
    }

    @Transactional
    public void updateDishStatus(Long dishId, AdminDishStatusUpdateRequest request) {
        requireAdmin();
        if (dishMapper.selectById(dishId) == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Dish not found");
        }
        String normalizedStatus = normalizeDishStatus(request.getStatus(), false);
        dishMapper.update(
                null,
                new LambdaUpdateWrapper<Dish>()
                        .eq(Dish::getId, dishId)
                        .set(Dish::getStatus, normalizedStatus)
        );
        syncDishLotteryPool(dishId, normalizedStatus);
    }

    public PageResult<AdminReviewItemVo> listReviews(String targetType, String status, String keyword, Integer page, Integer size) {
        requireAdmin();
        String normalizedTargetType = normalizeReviewTargetType(targetType);
        String normalizedStatus = normalizeReviewStatus(status);
        int currentPage = normalizePage(page);
        int currentSize = normalizeSize(size);
        long total = adminMapper.countAdminReviewList(normalizedTargetType, normalizedStatus, normalizeKeyword(keyword));
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0, currentPage, currentSize);
        }
        long offset = (long) (currentPage - 1) * currentSize;
        List<AdminReviewItemVo> records = adminMapper.selectAdminReviewList(
                normalizedTargetType,
                normalizedStatus,
                normalizeKeyword(keyword),
                offset,
                currentSize
        );
        decorateReviewImages(records);
        return PageResult.of(records, total, currentPage, currentSize);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        requireAdmin();
        reviewService.deleteReview(reviewId);
    }

    public AdminStatisticsOverviewVo getStatisticsOverview() {
        requireAdmin();
        return adminMapper.selectStatisticsOverview();
    }

    public List<AdminPopularDishVo> getPopularDishes(Integer limit) {
        requireAdmin();
        return adminMapper.selectPopularDishes(normalizeLimit(limit));
    }

    public List<AdminPopularMerchantVo> getPopularMerchants(Integer limit) {
        requireAdmin();
        return adminMapper.selectPopularMerchants(normalizeLimit(limit));
    }

    public List<AdminUserActivityVo> getUserActivity(LocalDate startDate, LocalDate endDate) {
        requireAdmin();
        LocalDate resolvedEndDate = endDate == null ? LocalDate.now() : endDate;
        LocalDate resolvedStartDate = startDate == null ? resolvedEndDate.minusDays(6) : startDate;
        if (resolvedStartDate.isAfter(resolvedEndDate)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "startDate cannot be after endDate");
        }
        return adminMapper.selectUserActivity(
                resolvedStartDate.atStartOfDay(),
                resolvedEndDate.plusDays(1).atStartOfDay()
        );
    }

    public List<AdminCanteenScoreVo> getCanteenScores() {
        requireAdmin();
        return adminMapper.selectCanteenScores();
    }

    private void decorateDishDetail(AdminDishDetailVo detail) {
        Long dishId = detail.getId();
        detail.setImages(buildDishImageMap(List.of(dishId)).getOrDefault(dishId, Collections.emptyList()));
        detail.setTags(buildDishTagMap(List.of(dishId)).getOrDefault(dishId, Collections.emptyList()));
    }

    private Map<Long, List<String>> buildDishImageMap(List<Long> dishIds) {
        List<DishImageRelationVo> relations = dishMapper.selectDishImagesByDishIds(dishIds);
        Map<Long, List<String>> imageMap = new LinkedHashMap<>();
        for (DishImageRelationVo relation : relations) {
            imageMap.computeIfAbsent(relation.getDishId(), key -> new ArrayList<>()).add(relation.getImageUrl());
        }
        return imageMap;
    }

    private Map<Long, List<DishTagVo>> buildDishTagMap(List<Long> dishIds) {
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

    private void syncDishLotteryPool(Long dishId, String dishStatus) {
        if (isActiveLotteryDishStatus(dishStatus)) {
            adminMapper.upsertDishLotteryPool(dishId);
            return;
        }
        adminMapper.updateDishLotteryPoolStatus(dishId, LOTTERY_POOL_STATUS_INACTIVE);
    }

    private boolean isActiveLotteryDishStatus(String status) {
        return DISH_STATUS_ON_SALE.equals(status) || DISH_STATUS_SOLD_OUT.equals(status);
    }

    private void decorateReviewImages(List<AdminReviewItemVo> records) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> reviewIds = records.stream().map(AdminReviewItemVo::getId).toList();
        Map<Long, List<String>> imageMap = buildImageMap(reviewIds);
        for (AdminReviewItemVo record : records) {
            record.setImages(imageMap.getOrDefault(record.getId(), Collections.emptyList()));
        }
    }

    private Map<Long, List<String>> buildImageMap(List<Long> reviewIds) {
        List<ReviewImageRelationVo> relations = reviewMapper.selectReviewImages(reviewIds);
        Map<Long, List<String>> imageMap = new LinkedHashMap<>();
        for (ReviewImageRelationVo relation : relations) {
            imageMap.computeIfAbsent(relation.getReviewId(), key -> new ArrayList<>()).add(relation.getImageUrl());
        }
        return imageMap;
    }

    private void syncMerchantStatusOnUserStateChange(Long userId, boolean disabled) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId).last("LIMIT 1")
        );
        if (merchant == null) {
            return;
        }
        String nextStatus;
        if (disabled) {
            nextStatus = MERCHANT_STATUS_DISABLED;
        } else if (APPLY_APPROVED.equals(merchant.getApplyStatus())) {
            nextStatus = MERCHANT_STATUS_OPEN;
        } else {
            nextStatus = MERCHANT_STATUS_PENDING;
        }
        merchantMapper.update(
                null,
                new LambdaUpdateWrapper<Merchant>()
                        .eq(Merchant::getId, merchant.getId())
                        .set(Merchant::getStatus, nextStatus)
        );
    }

    private SysUser getManageableUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || isAdminUser(userId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private Merchant getMerchantOrThrow(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商家申请不存在");
        }
        return merchant;
    }

    private void ensureCampusExists(Long campusId) {
        if (adminMapper.countCampusById(campusId) == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "校区不存在");
        }
    }

    private Canteen ensureCanteenExists(Long canteenId) {
        Canteen canteen = canteenMapper.selectById(canteenId);
        if (canteen == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "食堂/店铺不存在");
        }
        return canteen;
    }

    private boolean isAdminUser(Long userId) {
        return sysUserMapper.countUserRole(userId, RoleCode.ADMIN.name()) > 0;
    }

    private void ensureUserRole(Long userId, RoleCode roleCode) {
        if (sysUserMapper.countUserRole(userId, roleCode.name()) > 0) {
            return;
        }
        Long roleId = sysRoleMapper.selectIdByRoleCode(roleCode.name());
        if (roleId == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "角色数据缺失: " + roleCode.name());
        }
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        sysUserRoleMapper.insert(userRole);
    }

    private void validateCanteenUpdateRequest(AdminCanteenUpdateRequest request) {
        if (request.getCampusId() == null
                && request.getName() == null
                && request.getType() == null
                && request.getLocation() == null
                && request.getDescription() == null
                && request.getOpeningHours() == null
                && request.getStatus() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "更新内容不能为空");
        }
    }

    private void validateStallUpdateRequest(AdminStallUpdateRequest request) {
        if (request.getName() == null
                && request.getLocation() == null
                && request.getDescription() == null
                && request.getStatus() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "更新内容不能为空");
        }
    }

    private int normalizePage(Integer page) {
        return page == null ? 1 : page;
    }

    private int normalizeSize(Integer size) {
        return size == null ? 10 : Math.min(size, 50);
    }

    private int normalizeLimit(Integer limit) {
        return limit == null ? 10 : Math.min(limit, 50);
    }

    private String normalizeKeyword(String keyword) {
        return trimToNull(keyword);
    }

    private String normalizeUserRole(String role) {
        if (!StringUtils.hasText(role)) {
            return null;
        }
        String normalized = role.trim().toUpperCase();
        if (!ROLE_STUDENT.equals(normalized) && !ROLE_MERCHANT.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "role is invalid");
        }
        return normalized;
    }

    private String normalizeUserStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (!UserStatus.ACTIVE.name().equals(normalized) && !UserStatus.DISABLED.name().equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "status is invalid");
        }
        return normalized;
    }

    private String normalizeApplyStatus(String applyStatus) {
        if (!StringUtils.hasText(applyStatus)) {
            return null;
        }
        String normalized = applyStatus.trim().toUpperCase();
        if (!APPLY_PENDING.equals(normalized) && !APPLY_APPROVED.equals(normalized) && !APPLY_REJECTED.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "applyStatus is invalid");
        }
        return normalized;
    }

    private String normalizeCanteenType(String type) {
        String normalized = requireText(type, "type").toUpperCase();
        if (!CANTEEN_TYPE_CANTEEN.equals(normalized)
                && !CANTEEN_TYPE_CAMPUS_SHOP.equals(normalized)
                && !CANTEEN_TYPE_PERIPHERY_SHOP.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "type is invalid");
        }
        return normalized;
    }

    private String normalizeBusinessStatus(String status) {
        String normalized = requireText(status, "status").toUpperCase();
        if (!STATUS_OPEN.equals(normalized) && !STATUS_CLOSED.equals(normalized) && !STATUS_DISABLED.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "status is invalid");
        }
        return normalized;
    }

    private String normalizeReviewTargetType(String targetType) {
        if (!StringUtils.hasText(targetType)) {
            return null;
        }
        String normalized = targetType.trim().toUpperCase();
        if (!REVIEW_TARGET_DISH.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "targetType is invalid");
        }
        return normalized;
    }

    private String normalizeReviewStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (!REVIEW_STATUS_VISIBLE.equals(normalized)
                && !REVIEW_STATUS_HIDDEN.equals(normalized)
                && !REVIEW_STATUS_DELETED.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "status is invalid");
        }
        return normalized;
    }

    private String normalizeDishStatus(String status, boolean allowNull) {
        if (!StringUtils.hasText(status)) {
            if (allowNull) {
                return null;
            }
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

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void requireAdmin() {
        SecurityUtils.requireRole(RoleCode.ADMIN);
    }
}
