package com.eatnow.backend.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.feedback.entity.Feedback;
import com.eatnow.backend.feedback.vo.FeedbackItemVo;
import com.eatnow.backend.feedback.vo.FeedbackTargetRelationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    FeedbackTargetRelationVo selectVisibleDishTarget(@Param("dishId") Long dishId);

    long countMerchantFeedback(
            @Param("merchantId") Long merchantId,
            @Param("dishId") Long dishId,
            @Param("feedbackType") String feedbackType,
            @Param("status") String status
    );

    List<FeedbackItemVo> selectMerchantFeedbackList(
            @Param("merchantId") Long merchantId,
            @Param("dishId") Long dishId,
            @Param("feedbackType") String feedbackType,
            @Param("status") String status,
            @Param("offset") long offset,
            @Param("size") int size
    );
}
