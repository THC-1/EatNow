package com.eatnow.backend.llmrecommendation.mapper;

import com.eatnow.backend.llmrecommendation.dto.LlmRecommendationConstraints;
import com.eatnow.backend.llmrecommendation.vo.LlmCandidateDishVo;
import com.eatnow.backend.llmrecommendation.vo.LlmDishImageRelationVo;
import com.eatnow.backend.llmrecommendation.vo.LlmDishTagRelationVo;
import com.eatnow.backend.llmrecommendation.vo.LlmSignalWeightVo;
import com.eatnow.backend.llmrecommendation.vo.LlmUserPreferenceSnapshotVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LlmRecommendationMapper {

    LlmUserPreferenceSnapshotVo selectUserPreferenceSnapshot(@Param("userId") Long userId);

    List<LlmSignalWeightVo> selectUserTagSignals(@Param("userId") Long userId);

    List<LlmSignalWeightVo> selectUserCategorySignals(@Param("userId") Long userId);

    List<LlmSignalWeightVo> selectUserCanteenSignals(@Param("userId") Long userId);

    BigDecimal selectUserAveragePositivePrice(@Param("userId") Long userId);

    List<String> selectUserFavoriteDishNames(@Param("userId") Long userId);

    List<String> selectUserEatenDishNames(@Param("userId") Long userId);

    List<String> selectUserHighRatedDishNames(@Param("userId") Long userId);

    List<LlmCandidateDishVo> selectAvailableCandidateDishes(
            @Param("constraints") LlmRecommendationConstraints constraints,
            @Param("limit") int limit
    );

    List<LlmCandidateDishVo> selectPopularCandidateDishes(@Param("limit") int limit);

    List<LlmDishImageRelationVo> selectDishImages(@Param("dishIds") List<Long> dishIds);

    List<LlmDishTagRelationVo> selectDishTags(@Param("dishIds") List<Long> dishIds);
}
