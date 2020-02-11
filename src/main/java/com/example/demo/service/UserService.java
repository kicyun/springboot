package com.example.demo.service;

import com.example.demo.advice.exception.CEmailSigninFailedException;
import com.example.demo.entity.User;
import com.example.demo.repo.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserJpaRepo userJpaRepo;

    // 사용자 검색
    @Async("DatabaseThreadPoolTaskExecutor")
    public CompletableFuture<Optional<User>> findByUidAsync(String uid) throws InterruptedException, ExecutionException {
        Optional<User> user = userJpaRepo.findByUid(uid);
        return CompletableFuture
                .completedFuture(user);
    }

    // 사용자 저장
    @Async("DatabaseThreadPoolTaskExecutor")
    public void saveAsync (User user) {
        userJpaRepo.save(user);
    }
}
