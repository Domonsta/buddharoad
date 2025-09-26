package com.buddharoad.dto.Member; // 패키지 경로 확인!

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank; // jakarta.validation.constraints 임포트

// 이메일 형식 검사 등 추가 유효성 검사가 필요하면 아래 import도 추가
// import jakarta.validation.constraints.Pattern;
// import jakarta.validation.constraints.Size;

@Getter // Lombok: 모든 필드에 대한 getter 메서드 자동 생성
@NoArgsConstructor // Lombok: 인자 없는 기본 생성자 자동 생성
@AllArgsConstructor // Lombok: 모든 필드를 인자로 받는 생성자 자동 생성 (편의를 위해)
public class LoginRequestDTO { // 로그인 요청용 DTO

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String loginId; // 필드명: login_id -> loginId (Java 컨벤션)

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.") // 메시지 수정
    private String password;
}