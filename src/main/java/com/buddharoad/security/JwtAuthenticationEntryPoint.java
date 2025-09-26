package com.buddharoad.security;

import com.fasterxml.jackson.databind.ObjectMapper; // JSON 처리를 위해 ObjectMapper 추가
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // JSON 변환을 위한 ObjectMapper 주입 (또는 직접 생성)
    private final ObjectMapper objectMapper = new ObjectMapper(); // 스프링 빈으로 주입받는 것이 더 좋지만, 간단한 예시를 위해 직접 생성

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // HTTP 상태 코드 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 응답 컨텐츠 타입을 JSON으로 설정
        response.setContentType("application/json;charset=UTF-8");

        // 에러 메시지를 담을 Map 생성
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", "인증 정보가 유효하지 않거나 필요합니다."); // 더 구체적인 메시지
        errorDetails.put("path", request.getRequestURI()); // 어떤 경로에서 에러가 발생했는지 추가
        // 필요하다면, authException.getMessage() 등을 추가하여 디버깅 정보 제공 가능
        // errorDetails.put("debugMessage", authException.getMessage());

        // Map을 JSON 문자열로 변환하여 응답 스트림에 쓰기
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));

        System.err.println("🚨 JwtAuthenticationEntryPoint 호출됨 (경로: " + request.getRequestURI() + ", 예외: " + authException.getMessage() + ")");
    }
}