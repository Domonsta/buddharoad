package com.buddharoad.dto.Member;

import com.buddharoad.security.Role; // Role Enum의 정확한 패키지 경로로 변경해줘!
import com.buddharoad.domain.Member; // Member 엔티티 import

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // 모든 필드를 포함하는 생성자 자동 생성
import java.time.LocalDateTime; // createdAt 필드를 위해 추가

@Getter // 모든 필드에 대한 getter 메서드 자동 생성
@NoArgsConstructor // 인자 없는 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 자동 생성 (fromEntity에서 사용)
public class MemberResponseDTO {
    // Member 엔티티의 필드명과 일치시키거나, DTO에 맞게 변경 (ex: memberNo -> id)
    private Long memberNo; // 엔티티의 memberNo에 매핑
    private String loginId; // 로그인 ID
    private String email;
    private String username; // 닉네임
    private Role role; // 사용자 역할
    private LocalDateTime createdAt; // 가입일자
    private String accountStatus; // 계정 활성화 상태

    // fromEntity 메서드를 사용하여 Member 엔티티로부터 DTO 객체를 생성
    // 비밀번호 필드는 절대 포함하지 않아!
    public static MemberResponseDTO fromEntity(Member member) {
        return new MemberResponseDTO(
                member.getMemberNo(), // 엔티티의 getMemberNo() 호출
                member.getLoginId(),  // 엔티티의 getLoginId() 호출
                member.getEmail(),
                member.getDisplayName(), // 엔티티의 getUsername() (닉네임) 호출
                member.getRole(),
                member.getCreatedAt(),
                member.getAccountStatus()
        );
    }
}