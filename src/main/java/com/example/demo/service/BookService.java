package com.example.demo.service;

import com.example.demo.advice.exception.CUserNotFoundException;
import com.example.demo.entity.SearchHistory;
import com.example.demo.model.book.SearchHistoryResult;
import com.example.demo.model.book.SearchRankResult;
import com.example.demo.repo.SearchHistoryJpaRepo;
import com.example.demo.repo.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {
    private final UserJpaRepo userJpaRepo;
    private final SearchHistoryJpaRepo searchHistoryJpaRepo;
    private final RedisTemplate<String, String>  redisTemplate;
    private final String cacheKey = "book:rank";

    // 책 검색
    public void search(String uid, String keyword) {
        SearchHistory history = new SearchHistory(userJpaRepo.findByUid(uid).orElseThrow(CUserNotFoundException::new), keyword);
        searchHistoryJpaRepo.save(history);
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.incrementScore(cacheKey, keyword, 1);
    }

    // 검색 기록
    public List<SearchHistoryResult> searchHistory(String uid) {
        List<SearchHistory> searchHistoryList = searchHistoryJpaRepo.findByUser(userJpaRepo.findByUid(uid).orElseThrow(CUserNotFoundException::new));

        List<SearchHistoryResult> searchHistoryResultList = new ArrayList<>();

        for (SearchHistory history : searchHistoryList) {
            SearchHistoryResult searchHistoryResult = new SearchHistoryResult();
            searchHistoryResult.setKeyword(history.getKeyword());
            searchHistoryResult.setCratedAt(history.getCratedAt());

            searchHistoryResultList.add(searchHistoryResult);
        }
        return searchHistoryResultList;
    }

    // 검색 랭킹
    public List<SearchRankResult> searchRank() {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> rankingSet = zSetOperations.reverseRangeWithScores(cacheKey, 0, 9);
        List<SearchRankResult> searchRankResultList = new ArrayList<>();

        for (ZSetOperations.TypedTuple<String > rank : rankingSet) {
            SearchRankResult searchRankResult = new SearchRankResult();
            searchRankResult.setHit(rank.getScore());
            searchRankResult.setKeyword(rank.getValue());

            searchRankResultList.add(searchRankResult);
        }

        return searchRankResultList;

    }
}
