package com.eatnow.backend.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.feedback.entity.MerchantImprovementRecord;
import com.eatnow.backend.feedback.vo.MerchantImprovementItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MerchantImprovementRecordMapper extends BaseMapper<MerchantImprovementRecord> {

    long countMerchantImprovementRecords(
            @Param("merchantId") Long merchantId,
            @Param("dishId") Long dishId,
            @Param("feedbackId") Long feedbackId,
            @Param("status") String status
    );

    List<MerchantImprovementItemVo> selectMerchantImprovementRecords(
            @Param("merchantId") Long merchantId,
            @Param("dishId") Long dishId,
            @Param("feedbackId") Long feedbackId,
            @Param("status") String status,
            @Param("offset") long offset,
            @Param("size") int size
    );
}
