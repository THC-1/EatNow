package com.eatnow.backend.llmrecommendation.service;

import com.eatnow.backend.llmrecommendation.dto.LlmRecommendationConstraints;
import com.eatnow.backend.llmrecommendation.vo.LlmCandidateDishVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class LlmRecommendationScoringService {

    public List<LlmCandidateDishVo> score(
            List<LlmCandidateDishVo> candidates,
            LlmUserProfile profile,
            String message,
            LlmRecommendationConstraints constraints
    ) {
        int maxViews = candidates.stream().mapToInt(candidate -> safeInt(candidate.getViewCount())).max().orElse(0);
        int maxFavorites = candidates.stream().mapToInt(candidate -> safeInt(candidate.getFavoriteCount())).max().orElse(0);
        int maxComments = candidates.stream().mapToInt(candidate -> safeInt(candidate.getCommentCount())).max().orElse(0);
        for (LlmCandidateDishVo candidate : candidates) {
            double preferenceScore = preferenceScore(candidate, profile, message, constraints);
            double qualityScore = qualityScore(candidate);
            double hotScore = hotScore(candidate, maxViews, maxFavorites, maxComments);
            double priceScore = priceScore(candidate, profile, constraints);
            double freshnessScore = freshnessScore(candidate);
            double finalScore = preferenceScore * 0.35
                    + qualityScore * 0.25
                    + hotScore * 0.20
                    + priceScore * 0.10
                    + freshnessScore * 0.10;
            candidate.setSoftScore(round(finalScore));
        }
        return candidates;
    }

    private double preferenceScore(
            LlmCandidateDishVo candidate,
            LlmUserProfile profile,
            String message,
            LlmRecommendationConstraints constraints
    ) {
        double score = 0.35;
        if (profile.getDefaultCanteenId() != null && profile.getDefaultCanteenId().equals(candidate.getCanteenId())) {
            score += 0.18;
        }
        score += normalizedWeight(profile.safeCanteenWeights(), candidate.getCanteenId()) * 0.14;
        score += normalizedWeight(profile.safeCategoryWeights(), candidate.getCategoryId()) * 0.14;

        int matchedTags = 0;
        for (String tag : candidate.getTags()) {
            Double weight = profile.safeTagWeights().get(tag);
            if (weight != null && weight > 0) {
                matchedTags++;
                score += Math.min(weight / 20.0, 0.10);
            }
            if (profile.getExplicitTasteTags() != null && profile.getExplicitTasteTags().contains(tag)) {
                score += 0.08;
            }
            if (profile.getAvoidTags() != null && profile.getAvoidTags().contains(tag)) {
                score -= 0.35;
            }
        }
        if (matchedTags > 1) {
            score += 0.08;
        }
        if (messageMatches(candidate, message)) {
            score += 0.16;
        }
        if (constraints != null && constraints.getCategoryId() != null
                && constraints.getCategoryId().equals(candidate.getCategoryId())) {
            score += 0.08;
        }
        return clamp(score);
    }

    private double qualityScore(LlmCandidateDishVo candidate) {
        double reviewConfidence = Math.min(Math.log1p(safeInt(candidate.getCommentCount())) / Math.log(21), 1.0);
        double average = scoreOf(candidate.getScore());
        double taste = scoreOf(candidate.getTasteScore());
        double portion = scoreOf(candidate.getPortionScore());
        double value = scoreOf(candidate.getValueScore());
        return clamp((average * 0.45 + taste * 0.20 + portion * 0.15 + value * 0.20) * (0.75 + reviewConfidence * 0.25));
    }

    private double hotScore(LlmCandidateDishVo candidate, int maxViews, int maxFavorites, int maxComments) {
        double views = normalizedCount(safeInt(candidate.getViewCount()), maxViews);
        double favorites = normalizedCount(safeInt(candidate.getFavoriteCount()), maxFavorites);
        double comments = normalizedCount(safeInt(candidate.getCommentCount()), maxComments);
        return clamp(views * 0.30 + favorites * 0.45 + comments * 0.25);
    }

    private double priceScore(
            LlmCandidateDishVo candidate,
            LlmUserProfile profile,
            LlmRecommendationConstraints constraints
    ) {
        BigDecimal price = candidate.getPrice();
        if (price == null) {
            return 0.5;
        }
        BigDecimal minPrice = constraints != null && constraints.getMinPrice() != null
                ? constraints.getMinPrice()
                : profile.getMinPrice();
        BigDecimal maxPrice = constraints != null && constraints.getMaxPrice() != null
                ? constraints.getMaxPrice()
                : profile.getMaxPrice();
        if (minPrice != null && price.compareTo(minPrice) < 0) {
            return 0.35;
        }
        if (maxPrice != null && price.compareTo(maxPrice) > 0) {
            return 0.25;
        }
        if (profile.getAveragePositivePrice() == null || profile.getAveragePositivePrice().compareTo(BigDecimal.ZERO) <= 0) {
            return 0.85;
        }
        double base = profile.getAveragePositivePrice().doubleValue();
        double distance = Math.abs(price.doubleValue() - base) / Math.max(base, 1.0);
        return clamp(1.0 - Math.min(distance, 1.0) * 0.45);
    }

    private double freshnessScore(LlmCandidateDishVo candidate) {
        double score = StringUtils.hasText(candidate.getCurrentRecommendReason()) ? 0.75 : 0.35;
        if (candidate.getCreatedAt() != null
                && candidate.getCreatedAt().isAfter(LocalDateTime.now().minus(Duration.ofDays(30)))) {
            score += 0.25;
        }
        return clamp(score);
    }

    private boolean messageMatches(LlmCandidateDishVo candidate, String message) {
        if (!StringUtils.hasText(message)) {
            return false;
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        if (contains(normalized, candidate.getName())
                || contains(normalized, candidate.getDescription())
                || contains(normalized, candidate.getCategoryName())
                || contains(normalized, candidate.getCanteenName())
                || contains(normalized, candidate.getMerchantName())) {
            return true;
        }
        return candidate.getTags().stream().anyMatch(tag -> contains(normalized, tag));
    }

    private boolean contains(String haystack, String needle) {
        return StringUtils.hasText(needle) && haystack.contains(needle.toLowerCase(Locale.ROOT));
    }

    private double normalizedWeight(Map<Long, Double> weights, Long id) {
        if (id == null || weights.isEmpty()) {
            return 0.0;
        }
        Double value = weights.get(id);
        if (value == null || value <= 0) {
            return 0.0;
        }
        double max = weights.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        return Math.min(value / Math.max(max, 1.0), 1.0);
    }

    private double scoreOf(BigDecimal score) {
        return score == null ? 0.0 : clamp(score.doubleValue() / 5.0);
    }

    private double normalizedCount(int value, int max) {
        if (max <= 0) {
            return 0.0;
        }
        return Math.log1p(value) / Math.log1p(max);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double round(double value) {
        return BigDecimal.valueOf(value * 100)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
