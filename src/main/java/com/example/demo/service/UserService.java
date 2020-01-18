package com.example.demo.service;

import com.example.demo.advice.exception.CEmailSigninFailedException;
import com.example.demo.entity.User;
import com.example.demo.repo.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserJpaRepo userJpaRepo;

    // 사용자 검색
    @Async
    public CompletableFuture<User> findByUid(String id) {
        return CompletableFuture
                .completedFuture(userJpaRepo
                    .findByUid(id)
                    .orElseThrow(CEmailSigninFailedException::new));
    }

    // 사용자 저장
    @Async
    public void save(User user) {
        userJpaRepo.save(user);
    }
}
