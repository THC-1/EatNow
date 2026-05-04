package com.eatnow.backend.ranking.service;

import com.eatnow.backend.ranking.mapper.RankingMapper;
import com.eatnow.backend.ranking.vo.RankingItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingMapper rankingMapper;

    public List<RankingItemVo> topRated(Long canteenId, Long categoryId, Integer limit) {
        return decorate(rankingMapper.selectTopRated(canteenId, categoryId, normalizeLimit(limit)));
    }

    public List<RankingItemVo> popular(Long canteenId, Integer limit) {
        return decorate(rankingMapper.selectPopular(canteenId, normalizeLimit(limit)));
    }

    public List<RankingItemVo> mostFavorited(Long canteenId, Integer limit) {
        return decorate(rankingMapper.selectMostFavorited(canteenId, normalizeLimit(limit)));
    }

    public List<RankingItemVo> bestValue(Long canteenId, Integer limit) {
        return decorate(rankingMapper.selectBestValue(canteenId, normalizeLimit(limit)));
    }

    public List<RankingItemVo> newDishes(Long canteenId, Integer limit) {
        return decorate(rankingMapper.selectNewDishes(canteenId, normalizeLimit(limit)));
    }

    public List<RankingItemVo> mostFeedback(Long canteenId, Integer limit) {
        return decorate(rankingMapper.selectMostFeedback(canteenId, normalizeLimit(limit)));
    }

    private List<RankingItemVo> decorate(List<RankingItemVo> items) {
        for (int i = 0; i < items.size(); i++) {
            RankingItemVo item = items.get(i);
            item.setRank(i + 1);
            item.setImages(item.getCoverImageUrl() == null ? Collections.emptyList() : List.of(item.getCoverImageUrl()));
        }
        return items;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 10;
        }
        return Math.min(limit, 50);
    }
}
