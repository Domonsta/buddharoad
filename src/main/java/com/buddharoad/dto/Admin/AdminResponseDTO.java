// src/main/java/com/buddharoad/dto/Admin/AdminResponseDTO.java (새로 생성)
package com.buddharoad.dto.Admin; // Admin 관련 DTO 패키지

import com.buddharoad.domain.Member; // Member 엔티티 임포트
import com.buddharoad.security.Role; // Role Enum 임포트
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResponseDTO {
    private Long memberNo;
    private String loginId; // 아이디
    private String email;
    private String username; // 닉네임
    private Role role; // 관리자 역할
    private LocalDateTime createdAt; // 가입일

    public static AdminResponseDTO fromEntity(Member member) {
        return AdminResponseDTO.builder()
                .memberNo(member.getMemberNo())
                .loginId(member.getLoginId())
                .email(member.getEmail())
                .username(member.getDisplayName()) // Member 엔티티의 getDisplayName() (닉네임)
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .build();
    }
}