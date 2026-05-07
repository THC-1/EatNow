package com.eatnow.backend.llmrecommendation.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "eatnow.llm-recommendation", name = "mongo-enabled", havingValue = "true")
public class LlmRecommendationMongoConfig {

    private final LlmRecommendationProperties properties;

    @Bean
    public MongoClient llmRecommendationMongoClient() {
        return MongoClients.create(properties.getMongoUri());
    }

    @Bean
    public MongoTemplate llmRecommendationMongoTemplate(MongoClient llmRecommendationMongoClient) {
        return new MongoTemplate(llmRecommendationMongoClient, properties.getMongoDatabase());
    }
}
