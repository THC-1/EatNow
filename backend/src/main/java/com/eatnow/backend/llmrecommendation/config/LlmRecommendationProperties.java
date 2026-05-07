package com.eatnow.backend.llmrecommendation.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "eatnow.llm-recommendation")
public class LlmRecommendationProperties {

    private boolean enabled = false;
    private boolean mongoEnabled = false;
    private String mongoUri = "mongodb://localhost:27017/eatnow_llm";
    private String mongoDatabase = "eatnow_llm";
    private String provider = "openai-compatible";
    private String baseUrl = "https://api.openai.com/v1";
    private String apiKey;
    private String model = "gpt-4o-mini";

    @Min(1)
    private int profileTtlMinutes = 60;

    @Min(10)
    private int recallSize = 80;

    @Min(5)
    private int sampleSize = 20;

    @Min(1)
    private int llmTimeoutSeconds = 20;
}
