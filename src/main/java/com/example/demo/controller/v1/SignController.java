package com.example.demo.controller.v1;

import com.example.demo.advice.exception.CEmailSigninFailedException;
import com.example.demo.config.security.JwtTokenProvider;
import com.example.demo.entity.User;
import com.example.demo.model.response.CommonResult;
import com.example.demo.model.response.SingleResult;
import com.example.demo.service.ResponseService;
import com.example.demo.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Api(tags = {"1. Sign"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class SignController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResponseService responseService;
    private final PasswordEncoder passwordEncoder;

    @ApiOperation(value = "이메일 로그인", notes = "이메일 회원 로그인")
    @PostMapping(value = "/signin")
    public SingleResult<String> signin(@ApiParam(value = "회원ID : 이메일", required = true) @RequestParam String id,
                                       @ApiParam(value = "비밀번호", required = true) @RequestParam String password) throws InterruptedException, ExecutionException {
        CompletableFuture<Optional<User>> userFuture = userService.findByUidAsync(id);
        CompletableFuture.allOf(userFuture).join();
        User user = userFuture.get()
                .orElseThrow(CEmailSigninFailedException::new);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CEmailSigninFailedException();
        }
        return responseService.getSingleResult(jwtTokenProvider.createToken(String.valueOf(user.getMsrl()), user.getRoles()));
    }

    @ApiOperation(value = "이메일 회원가입", notes = "이메일 회원 가입")
    @PostMapping(value = "/signup")
    public CommonResult signup(@ApiParam(value = "회원ID : 이메일", required = true) @RequestParam String id,
                               @ApiParam(value = "비밀번호", required = true) @RequestParam String password,
                               @ApiParam(value = "이름",required = true) @RequestParam String name) throws ExecutionException, InterruptedException {
        userService.findByUidAsync(id).thenAcceptAsync(user -> {
            if (user.isPresent() == true) {
                // 이미 가입한 가입자의 경우 로그를 남기자
                log.info("Already Exist User : " + user.get().getUid());
            } else {
                // 가입시키자.
                userService.saveAsync(
                        User.builder()
                                .uid(id)
                                .password(passwordEncoder.encode(password))
                                .name(name)
                                .roles(Collections.singletonList("ROLE_USER"))
                                .build());
            }
        });

        // 저장이 되든 말든 성공으로 리턴해서 해커 공격 시 가입여부를 알려주지 않는다.
        return responseService.getSuccessResult();
    }
}
