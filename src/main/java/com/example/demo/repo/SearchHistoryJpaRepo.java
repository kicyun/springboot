package com.example.demo.repo;

import com.example.demo.entity.SearchHistory;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryJpaRepo extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findByUser(User user);
}
