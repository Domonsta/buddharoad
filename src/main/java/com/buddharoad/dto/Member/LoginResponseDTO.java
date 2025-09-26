package com.buddharoad.dto.Member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken; // JWT (Access Token)
    private String loginId;     // 로그인 ID
    private String username;    // 닉네임 - 00님 환영합니다 로 받을 때 필요
    private String message;     // 로그인 성공 메시지 등
}
