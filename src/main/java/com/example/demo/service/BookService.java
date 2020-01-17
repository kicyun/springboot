package com.example.demo.service;

import com.example.demo.advice.exception.CUserNotFoundException;
import com.example.demo.entity.History;
import com.example.demo.repo.HistoryJpaRepo;
import com.example.demo.repo.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {
    private final UserJpaRepo userJpaRepo;
    private final HistoryJpaRepo historyJpaRepo;
    // 책 검색
    public void search(String uid, String keyword) {
        History history = new History(userJpaRepo.findByUid(uid).orElseThrow(CUserNotFoundException::new), keyword);
        historyJpaRepo.save(history);
    }

    // 검색 기록
    public List<History> history(String uid) {
        return historyJpaRepo.findByUser(userJpaRepo.findByUid(uid).orElseThrow(CUserNotFoundException::new));
    }

    // 검색 랭킹
    public void rank() {

    }
}
