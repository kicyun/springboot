package com.example.demo.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable()  // 비인증시 로그인 화면으로 이동하는 기본 설정 사용 안 함(rest api 이므로)
                .csrf().disable()   // csrf 보안 필요 없음(rest api 이므로)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 인증 체계가 jwt token 기반이라 세션을 필요 없음
                .and()
                    .authorizeRequests() // 사용 권한 확인
                        .antMatchers("/*/signin", "/*/signin/**", "/*/signup", "/*/signup/**").permitAll()  // 가입 및 인증은 모두 접근 가능하게
                        .antMatchers(HttpMethod.GET, "/exception/**", "/ping", "/v1/search/book/rank").permitAll()    // ping(헬쓰 체크) 도 모두 접근 가능하게
                        .anyRequest().hasRole("USER") // 그 외 나머지 요청은 모두 인증된 회원만 접근 가능하게
                .and()
                    .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .and()
                    .exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler())
                .and()
                    .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/v2/api-docs", "/swagger-resources/**",
                "/swagger-ui.html", "/webjars/**", "/swagger/**");
    }
}
