package com.logilink.company.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (API 서버)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 완전 비활성화 (JWT + Gateway 구조에서는 불필요)
                .sessionManagement(session -> session.disable())

                // 게이트웨이에서 인증 후 통과된 요청만 허용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/api/v1/hub-routes/path", // 알고리즘 경로 탐색은 공개
                                "/actuator/health"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
