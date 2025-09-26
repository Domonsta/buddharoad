// src/main/java/com/buddharoad/service/Member/MemberService.java (인터페이스 보강)
package com.buddharoad.service.Member;

import com.buddharoad.dto.Member.*;
import com.buddharoad.security.Role;
import com.buddharoad.domain.Member;

import org.springframework.data.domain.Page; // ⭐ Page 임포트

public interface MemberService {
    // ... (기존 메서드들 유지)
    MemberResponseDTO signUp(MemberRequestDTO memberRequestDto);
    LoginResponseDTO login(LoginRequestDTO loginRequestDto);
    MemberResponseDTO getMyInfo(String loginId);
    MemberResponseDTO getMemberByLoginId(String loginId); // MemberResponseDTO 반환
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    void updateMemberInfo(String loginId, MemberUpdateDTO updateDTO);
    void withdrawMember(String loginId, String password); // 사용자 본인 탈퇴용
    boolean checkEmailDuplicationForUpdate(String email, String currentLoginId);
    boolean checkUsernameDuplicationForUpdate(String username, String currentLoginId);
    Member getMemberEntityByLoginId(String loginId); // Member 엔티티 반환

    // ⭐⭐ 추가: 관리자용 회원 목록 조회 및 검색
    Page<MemberResponseDTO> getAllMembers(MemberSearchFilterDTO filterDTO);

    // ⭐⭐ 추가: 관리자용 회원 상태 변경 (활성/정지/차단)
    void updateMemberStatus(Long memberNo, String newStatus, Role currentAdminRole);

    // ⭐⭐ 추가: 관리자용 회원 소프트 삭제 (accountStatus 변경)
    void adminDeleteMember(Long memberNo, Role currentAdminRole);

    // ⭐ 이 메서드 선언을 추가해야 합니다.
    Role getMemberRole(Long memberNo);
}