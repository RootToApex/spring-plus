package org.example.expert.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {   // FilterConfig 클래스를 대신하는 역할로 클래스 삭제

    private final JwtSecurityFilter jwtSecurityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth    // URL 별로 접근 권한 설정
                        .requestMatchers("/auth/**").permitAll()    // /auth/signup, /auth/signin 같은 URL은 누구나 접근 가능
                        .requestMatchers("/admin/**").hasAuthority("ADMIN") // /admin/** URL은 ADMIN 권한을 가진 유저만 접근 가능, 기존 JwtFilter에서 직접 체크하던 걸 Security가 대신함
                        .anyRequest().authenticated())  // 나머지 모든 URL은 로그인한 유저만 접근 가능
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다."))
                )
                .addFilterBefore(jwtSecurityFilter, SecurityContextHolderAwareRequestFilter.class)  // JWT 필터를 Security 필터 체인에 등록
                .build();
    }
}