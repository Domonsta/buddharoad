package com.buddharoad.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends GenericFilterBean { // 모든 요청에 대해 JWT 토큰을 검사하고 유효하면 SecurityContext에 인증 정보 저장

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = null; // 토큰 변수를 try 블록 밖에서 선언

        try {
            // 1. Request Header에서 JWT 토큰 추출
            token = jwtTokenProvider.resolveToken(httpServletRequest);

            // 토큰이 존재하는 경우에만 유효성 검사 시도
            if (token != null) {
                // 2. validateToken으로 토큰 유효성 검사
                if (jwtTokenProvider.validateToken(token)) {
                    // 토큰이 유효할 경우 토큰에서 Authentication 객체를 가지고 와서 SecurityContext 에 저장
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // JWT 토큰 처리 중 발생한 모든 예외를 여기서 처리.
            // 예외를 밖으로 던지지 않고 로그만 남기고 다음 필터로 진행.
            // 이렇게 해야 permmitAll() 경로에서 유효하지 않은 토큰이 있어도 401이 발생하지 않음.
            System.err.println("🚨 JWT 토큰 처리 중 예외 발생 (경로: " + httpServletRequest.getRequestURI() + ", 예외: " + e.getMessage() + ")");
            // SecurityContext에 인증 정보를 설정하지 않고 다음 필터로 진행.
            // 보호된 경로라면 다음 필터(FilterSecurityInterceptor)에서 401 또는 403을 발생시킬 것임.
            // permmitAll() 경로라면 인증 정보 없어도 통과될 것임.
        }
        chain.doFilter(request, response); // 다음 필터로 진행
    }
}
