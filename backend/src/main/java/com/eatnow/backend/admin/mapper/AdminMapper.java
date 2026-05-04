package com.eatnow.backend.admin.mapper;

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
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminMapper {

    long countAdminUserList(
            @Param("role") String role,
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    List<AdminUserListItemVo> selectAdminUserList(
            @Param("role") String role,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") int size
    );

    AdminUserDetailVo selectAdminUserDetail(@Param("id") Long id);

    AdminStudentProfileVo selectStudentProfile(@Param("userId") Long userId);

    AdminMerchantProfileVo selectMerchantProfile(@Param("userId") Long userId);

    long countMerchantApplications(@Param("applyStatus") String applyStatus);

    List<AdminMerchantApplicationItemVo> selectMerchantApplications(
            @Param("applyStatus") String applyStatus,
            @Param("offset") long offset,
            @Param("size") int size
    );

    long countAdminReviewList(
            @Param("targetType") String targetType,
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    List<AdminReviewItemVo> selectAdminReviewList(
            @Param("targetType") String targetType,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") int size
    );

    long countCampusById(@Param("id") Long id);

    long countStallsByCanteenId(@Param("canteenId") Long canteenId);

    long countMerchantByStallId(@Param("stallId") Long stallId);

    long countDishByStallId(@Param("stallId") Long stallId);

    long countAdminDishList(
            @Param("merchantId") Long merchantId,
            @Param("canteenId") Long canteenId,
            @Param("stallId") Long stallId,
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    List<AdminDishItemVo> selectAdminDishList(
            @Param("merchantId") Long merchantId,
            @Param("canteenId") Long canteenId,
            @Param("stallId") Long stallId,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") int size
    );

    AdminDishDetailVo selectAdminDishDetail(@Param("id") Long id);

    int upsertDishLotteryPool(@Param("dishId") Long dishId);

    int updateDishLotteryPoolStatus(
            @Param("dishId") Long dishId,
            @Param("status") String status
    );

    AdminStatisticsOverviewVo selectStatisticsOverview();

    List<AdminPopularDishVo> selectPopularDishes(@Param("limit") int limit);

    List<AdminPopularMerchantVo> selectPopularMerchants(@Param("limit") int limit);

    List<AdminUserActivityVo> selectUserActivity(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<AdminCanteenScoreVo> selectCanteenScores();
}
