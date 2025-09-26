package com.buddharoad.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper; // JSON 처리를 위해 ObjectMapper 추가
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    // JSON 변환을 위한 ObjectMapper 주입 (또는 직접 생성)
    private final ObjectMapper objectMapper = new ObjectMapper(); // 스프링 빈으로 주입받는 것이 더 좋지만, 간단한 예시를 위해 직접 생성

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        // HTTP 상태 코드 설정
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 응답 컨텐츠 타입을 JSON으로 설정
        response.setContentType("application/json;charset=UTF-8");

        // 에러 메시지를 담을 Map 생성
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", HttpServletResponse.SC_FORBIDDEN);
        errorDetails.put("error", "Forbidden");
        errorDetails.put("message", "해당 리소스에 접근할 권한이 없습니다."); // 더 구체적인 메시지
        errorDetails.put("path", request.getRequestURI()); // 어떤 경로에서 에러가 발생했는지 추가

        // Map을 JSON 문자열로 변환하여 응답 스트림에 쓰기
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}