package com.eatnow.backend.llmrecommendation.mongo;

import com.eatnow.backend.llmrecommendation.service.LlmUserProfile;
import com.eatnow.backend.llmrecommendation.vo.MerchantDailyAnalysisSnapshot;
import com.eatnow.backend.llmrecommendation.vo.LlmRecommendationItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "eatnow.llm-recommendation", name = "mongo-enabled", havingValue = "true")
public class MongoRecommendationStore {

    private final MongoTemplate mongoTemplate;

    public Optional<LlmUserProfile> findUserProfile(Long userId, String role) {
        UserProfileDocument document = mongoTemplate.findById(buildProfileId(userId, role), UserProfileDocument.class);
        return Optional.ofNullable(document == null ? null : document.toProfile());
    }

    public void saveUserProfile(LlmUserProfile profile) {
        mongoTemplate.save(UserProfileDocument.from(profile));
    }

    public Optional<ConversationDocument> findConversation(String conversationId, Long userId, String role) {
        Query query = Query.query(Criteria.where("_id").is(conversationId)
                .and("userId").is(userId)
                .and("role").is(role));
        return Optional.ofNullable(mongoTemplate.findOne(query, ConversationDocument.class));
    }

    public void saveConversation(ConversationDocument conversation) {
        mongoTemplate.save(conversation);
    }

    public Optional<MerchantDailyReportDocument> findMerchantDailyReport(Long merchantId, java.time.LocalDate reportDate) {
        return Optional.ofNullable(mongoTemplate.findById(buildMerchantDailyReportId(merchantId, reportDate), MerchantDailyReportDocument.class));
    }

    public void saveMerchantDailyReport(MerchantDailyAnalysisSnapshot snapshot) {
        mongoTemplate.save(MerchantDailyReportDocument.from(snapshot));
    }

    public static String buildProfileId(Long userId, String role) {
        return role + ":" + userId;
    }

    public static String buildMerchantDailyReportId(Long merchantId, java.time.LocalDate reportDate) {
        return merchantId + ":" + reportDate;
    }

    @Document("llm_user_profiles")
    public static class UserProfileDocument {

        @Id
        private String id;
        private LlmUserProfile profile;

        public static UserProfileDocument from(LlmUserProfile profile) {
            UserProfileDocument document = new UserProfileDocument();
            document.id = buildProfileId(profile.getUserId(), profile.getRole());
            document.profile = profile;
            return document;
        }

        public LlmUserProfile toProfile() {
            return profile;
        }
    }

    @Document("llm_recommendation_conversations")
    public static class ConversationDocument {

        @Id
        private String id;
        private Long userId;
        private String role;
        private List<MessageDocument> messages = new ArrayList<>();
        private List<LlmRecommendationItemVo> lastRecommendations = new ArrayList<>();
        private String lastAction;
        private String conversationSummary;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public List<MessageDocument> getMessages() {
            return messages;
        }

        public void setMessages(List<MessageDocument> messages) {
            this.messages = messages;
        }

        public List<LlmRecommendationItemVo> getLastRecommendations() {
            return lastRecommendations;
        }

        public void setLastRecommendations(List<LlmRecommendationItemVo> lastRecommendations) {
            this.lastRecommendations = lastRecommendations;
        }

        public String getLastAction() {
            return lastAction;
        }

        public void setLastAction(String lastAction) {
            this.lastAction = lastAction;
        }

        public String getConversationSummary() {
            return conversationSummary;
        }

        public void setConversationSummary(String conversationSummary) {
            this.conversationSummary = conversationSummary;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class MessageDocument {

        private String role;
        private String content;
        private LocalDateTime createdAt;

        public MessageDocument() {
        }

        public MessageDocument(String role, String content, LocalDateTime createdAt) {
            this.role = role;
            this.content = content;
            this.createdAt = createdAt;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    @Document("llm_merchant_daily_reports")
    public static class MerchantDailyReportDocument {

        @Id
        private String id;
        private Long merchantId;
        private java.time.LocalDate reportDate;
        private MerchantDailyAnalysisSnapshot snapshot;
        private LocalDateTime updatedAt;

        public static MerchantDailyReportDocument from(MerchantDailyAnalysisSnapshot snapshot) {
            MerchantDailyReportDocument document = new MerchantDailyReportDocument();
            document.id = buildMerchantDailyReportId(snapshot.getMerchantId(), snapshot.getReportDate());
            document.merchantId = snapshot.getMerchantId();
            document.reportDate = snapshot.getReportDate();
            document.snapshot = snapshot;
            document.updatedAt = LocalDateTime.now();
            return document;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Long getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(Long merchantId) {
            this.merchantId = merchantId;
        }

        public java.time.LocalDate getReportDate() {
            return reportDate;
        }

        public void setReportDate(java.time.LocalDate reportDate) {
            this.reportDate = reportDate;
        }

        public MerchantDailyAnalysisSnapshot getSnapshot() {
            return snapshot;
        }

        public void setSnapshot(MerchantDailyAnalysisSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
