package com.example.demo.repo;

import com.example.demo.entity.History;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryJpaRepo extends JpaRepository<History, Long> {
    List<History> findByUser(User user);
}
