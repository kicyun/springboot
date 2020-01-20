package com.example.demo.repo;

import com.example.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.is;

@DataJpaTest
class UserJpaRepoTest {
    @Autowired
    private UserJpaRepo userJpaRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void whenFindByUid_thenReturnUser() throws InterruptedException, ExecutionException {
        String uid = "godoftest@test.com";
        String name = "godoftest";
        userJpaRepo.save(User.builder()
                .uid(uid)
                .password(passwordEncoder.encode("test"))
                .name(name)
                .roles(Collections.singletonList("ROLE_USER"))
                .build()
        );
        CompletableFuture<Optional<User>> userFuture = userJpaRepo.findByUid(uid);
        userFuture.allOf(userFuture).join();
        Optional<User> user = userFuture.get();
        assertNotNull(user);
        assertTrue(user.isPresent());
        assertEquals(user.get().getName(), name);
        assertThat(user.get().getName(), is(name));

    }
}