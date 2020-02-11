package com.example.demo.service;

import com.example.demo.advice.exception.CUserNotFoundException;
import com.example.demo.entity.SearchHistory;
import com.example.demo.entity.User;
import com.example.demo.model.book.SearchHistoryResult;
import com.example.demo.model.book.SearchRankResult;
import com.example.demo.repo.SearchHistoryJpaRepo;
import com.example.demo.repo.UserJpaRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
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

    private Object searchKakaoBook(String keyword, int page) throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + KAKAO_REST_API_KEY);
        final HttpEntity<String> entity = new HttpEntity(headers);

        final String url = KAKAO_API_BOOK_URL + "?query=" + keyword + "&page=" + page;
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object>responseData = mapper.readValue(responseEntity.getBody(), new TypeReference<Map<String, Object>>() {
        });

        HashMap<String, Object> result = new LinkedHashMap();
        result.put("meta", responseData.get("meta"));
        result.put("books", responseData.get("documents"));
        return result;
    }

    private Object searchNaverBook(String keyword, int page) throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", NAVER_API_CLIENT_ID);
        headers.set("X-Naver-Client-Secret", NAVER_API_CLIENT_SECRET);
        final HttpEntity<String> entity = new HttpEntity(headers);
        final int size = 10;

        final String url = NAVER_API_BOOK_URL + "?query=" + keyword + "&start=" + (1 + (page-1) * 10) + "&size=" + size;
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object>responseData = mapper.readValue(responseEntity.getBody(), new TypeReference<Map<String, Object>>() {
        });

        Integer total = (Integer)responseData.get("total");
        Integer start = (Integer)responseData.get("start");
        Integer pageable_count = total > 1000? 1000 : total; // 1000 껀 까지만 페이징 됨 (page는 100 까지 입력 가능. 넘으면 에러 발생) ;;;

        HashMap<String, Object> resultData = new LinkedHashMap<>();
        HashMap<String, Object> meta = new LinkedHashMap<>();


        // 카카오 meta 정보와 맞춰주자.
        meta.put("is_end", pageable_count > start + size - 1? false : true);
        meta.put("pageable_count", pageable_count);
        meta.put("total_count", total);

        resultData.put("meta", meta);
        resultData.put("books", responseData.get("items"));
        return resultData;
    }

    // 책 검색
    @HystrixCommand(fallbackMethod = "fallbackSearch")
    public Object search(String keyword, int page) throws Exception {
        return searchKakaoBook(keyword, page);
    }

    public Object fallbackSearch(String keyword, int page) throws Exception {
        return searchNaverBook(keyword, page);
    }

    // 검색 기록 저장
    @Async("DatabaseThreadPoolTaskExecutor")
    public void saveSearchHistoryAsync(String uid, String keyword) throws InterruptedException, ExecutionException {

        Optional<User> user = userJpaRepo.findByUid(uid);
        SearchHistory history = new SearchHistory(user.orElseThrow(CUserNotFoundException::new), keyword);
        searchHistoryJpaRepo.save(history);
    }

    // 검색 기록 검색
    @Async("DatabaseThreadPoolTaskExecutor")
    public CompletableFuture<List<SearchHistoryResult>> getSearchHistoryAsync(String uid) throws InterruptedException, ExecutionException {
        Optional<User> user = userJpaRepo.findByUid(uid);

        List<SearchHistory> searchHistoryList = searchHistoryJpaRepo
                .findByUserOrderByCratedAtDesc(
                        user.orElseThrow(CUserNotFoundException::new));

        List<SearchHistoryResult> searchHistoryResultList = new ArrayList<>();

        for (SearchHistory history : searchHistoryList) {
            SearchHistoryResult searchHistoryResult = new SearchHistoryResult();
            searchHistoryResult.setKeyword(history.getKeyword());
            searchHistoryResult.setCratedAt(history.getCratedAt());

            searchHistoryResultList.add(searchHistoryResult);
        }
        return CompletableFuture.completedFuture(searchHistoryResultList);
    }

    // 키워드 검색 수 증가
    @Async
    public void incrementSearchCountAsync(String keyword) {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.incrementScore(cacheKey, keyword, 1);
    }

    // 검색 랭킹
    @Async
    public CompletableFuture<List<SearchRankResult>> getSearchRankAsync() {
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
