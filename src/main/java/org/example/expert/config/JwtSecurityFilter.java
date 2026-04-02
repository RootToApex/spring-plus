package org.example.expert.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Authorization 헤더에서 JWT 토큰 꺼내기
        String bearerJwt = request.getHeader("Authorization");

        // 토큰이 없으면 400에러
        if (bearerJwt == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JWT 토큰이 필요합니다.");
            return;
        }

        // Bearer 부분 제거하고 순수 토큰값만 추출
        String jwt = jwtUtil.substringToken(bearerJwt);

        try {
            // JWT 에서 claims(유저 정보) 추출
            Claims claims = jwtUtil.extractClaims(jwt);

            // claims에서 필요한 유저 정보 꺼내기
            String email = claims.get("email", String.class);
            UserRole userRole = UserRole.of(claims.get("userRole", String.class));
            Long userId = Long.parseLong(claims.getSubject());

            // Security에서 사용하는 인증 객체 생성
            // 파라미터: 유저정보, 비밀번호(JWT 방식은 불필요하므로 null), 권한 목록
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            new org.example.expert.domain.common.dto.AuthUser(userId, email, userRole),
                            null,
                            List.of(new SimpleGrantedAuthority(userRole.name()))
                    );

            // 인증 정보 저장 (이후 필터나 컨트롤러에서 꺼내 쓸 수 있음)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 다음 필터로 요청 전달
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT 인증 오류", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않는 JWT 토큰입니다.");
        }
    }

    // /auth로 시작하는 URL은 이 필터를 건너뜀 (로그인/회원가입은 토큰이 불필요함)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String url = request.getRequestURI();
        return url.startsWith("/auth");
    }
}