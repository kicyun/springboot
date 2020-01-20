package com.example.demo.service;

import com.example.demo.advice.exception.CBookSearchFailedException;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    @Value("${naver.api.book.url}")
    private String NAVER_API_BOOK_URL;
    @Value("${naver.api.client.id}")
    private String NAVER_API_CLIENT_ID;
    @Value("${naver.api.client.secret}")
    private String NAVER_API_CLIENT_SECRET;

    private String searchKakaoBook(String keyword, int page) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + KAKAO_REST_API_KEY);
        final HttpEntity<String> entity = new HttpEntity(headers);

        final String url = KAKAO_API_BOOK_URL + "?query=" + keyword + "&page=" + page;
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return responseEntity.getBody();
    }

    private String searchNaverBook(String keyword, int page) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", NAVER_API_CLIENT_ID);
        headers.set("X-Naver-Client-Secret", NAVER_API_CLIENT_SECRET);
        final HttpEntity<String> entity = new HttpEntity(headers);

        final String url = NAVER_API_BOOK_URL + "?query=" + keyword + "&start=" + (1 + (page-1) * 10) + "&size=10";
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return responseEntity.getBody();
    }

    // 책 검색
    @Async
    public CompletableFuture<String> search(String keyword, int page) throws Exception {
        try {
            return CompletableFuture.completedFuture(searchKakaoBook(keyword, page));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(searchNaverBook(keyword, page));
        }
    }

    // 검색 기록 저장
    @Async
    public void saveSearchHistory(String uid, String keyword) {

        SearchHistory history = new SearchHistory(userJpaRepo.findByUid(uid).orElseThrow(CUserNotFoundException::new), keyword);
        searchHistoryJpaRepo.save(history);
    }

    // 키워드 검색 수 증가
    @Async
    public void incrementSearchCount(String keyword) {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.incrementScore(cacheKey, keyword, 1);
    }

    // 검색 기록 검색
    @Async
    public CompletableFuture<List<SearchHistoryResult>> getSearchHistory(String uid) throws InterruptedException, ExecutionException {
        CompletableFuture<List<SearchHistory>> searchHistoryListFuture = searchHistoryJpaRepo
                .findByUserOrderByCratedAtDesc(
                        userJpaRepo.findByUid(uid)
                                .orElseThrow(CUserNotFoundException::new));

        searchHistoryListFuture.allOf(searchHistoryListFuture).join();
        List<SearchHistory> searchHistoryList = searchHistoryListFuture.get();

        List<SearchHistoryResult> searchHistoryResultList = new ArrayList<>();

        for (SearchHistory history : searchHistoryList) {
            SearchHistoryResult searchHistoryResult = new SearchHistoryResult();
            searchHistoryResult.setKeyword(history.getKeyword());
            searchHistoryResult.setCratedAt(history.getCratedAt());

            searchHistoryResultList.add(searchHistoryResult);
        }
        return CompletableFuture.completedFuture(searchHistoryResultList);
    }

    // 검색 랭킹
    @Async
    public CompletableFuture<List<SearchRankResult>> getSearchRank() {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> rankingSet = zSetOperations.reverseRangeWithScores(cacheKey, 0, 9);
        List<SearchRankResult> searchRankResultList = new ArrayList<>();

        for (ZSetOperations.TypedTuple<String > rank : rankingSet) {
            SearchRankResult searchRankResult = new SearchRankResult();
            searchRankResult.setCount(rank.getScore());
            searchRankResult.setKeyword(rank.getValue());

            searchRankResultList.add(searchRankResult);
        }

        return CompletableFuture.completedFuture(searchRankResultList);

    }
}
