package com.example.demo.repo;

import com.example.demo.entity.SearchHistory;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SearchHistoryJpaRepo extends JpaRepository<SearchHistory, Long> {
    @Async
    CompletableFuture<List<SearchHistory>> findByUserOrderByCratedAtDesc(User user);
}
