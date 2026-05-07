package com.eatnow.backend.llmrecommendation.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LlmRecommendationChatVo {

    private String conversationId;
    private String action;
    private String reply;
    private List<LlmRecommendationItemVo> recommendations;
    private Boolean fallbackUsed;
    private String fallbackReason;
    private String conversationSummary;
    private String longTermMemory;
    private String shortTermMemory;
    private Boolean memoryUpdated;
    private LocalDateTime profileUpdatedAt;
}
