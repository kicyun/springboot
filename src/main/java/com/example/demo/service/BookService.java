package com.example.demo.service;

import com.example.demo.advice.exception.CUserNotFoundException;
import com.example.demo.entity.SearchHistory;
import com.example.demo.model.book.SearchHistoryResult;
import com.example.demo.model.book.SearchRankResult;
import com.example.demo.repo.SearchHistoryJpaRepo;
import com.example.demo.repo.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {
    private final UserJpaRepo userJpaRepo;
    private final SearchHistoryJpaRepo searchHistoryJpaRepo;
    private final RedisTemplate<String, String>  redisTemplate;
    private final RestTemplate restTemplate;

    private final String cacheKey = "book:search:rank";

    @Value("${kakao.api.book.url}")
    private String KAKAO_API_BOOK_URL;
    @Value("${kakao.api.key}")
    private String KAKAO_REST_API_KEY;

    // 책 검색
    public String search(String keyword, int page) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + KAKAO_REST_API_KEY);
        final HttpEntity<String> entity = new HttpEntity(headers);

        final String url = KAKAO_API_BOOK_URL + "?query=" + keyword + "&page=" + page;
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // 검색 기록 저장
    public void saveSearchHistory(String uid, String keyword) {

        SearchHistory history = new SearchHistory(userJpaRepo.findByUid(uid).orElseThrow(CUserNotFoundException::new), keyword);
        searchHistoryJpaRepo.save(history);
    }

    // 키워드 검색 수 증가
    public void incrementSearchCount(String keyword) {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.incrementScore(cacheKey, keyword, 1);
    }

    // 검색 기록 검색
    public List<SearchHistoryResult> getSearchHistory(String uid) {
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
    public List<SearchRankResult> getSearchRank() {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> rankingSet = zSetOperations.reverseRangeWithScores(cacheKey, 0, 9);
        List<SearchRankResult> searchRankResultList = new ArrayList<>();

        for (ZSetOperations.TypedTuple<String > rank : rankingSet) {
            SearchRankResult searchRankResult = new SearchRankResult();
            searchRankResult.setCount(rank.getScore());
            searchRankResult.setKeyword(rank.getValue());

            searchRankResultList.add(searchRankResult);
        }

        return searchRankResultList;

    }
}
