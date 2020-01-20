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
    @Async
    public CompletableFuture<Optional<User>> findByUid(String uid) throws InterruptedException, ExecutionException {
        CompletableFuture<Optional<User>> userFuture = userJpaRepo.findByUid(uid);
        CompletableFuture.allOf(userFuture).join();
        Optional<User> user = userFuture.get();
        return CompletableFuture
                .completedFuture(user);
                    //.orElseThrow(CEmailSigninFailedException::new));
    }

    // 사용자 저장
    @Async
    public void save(User user) {
        userJpaRepo.save(user);
    }
}
