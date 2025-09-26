// src/main/java/com/buddharoad/service/Member/MemberServiceImpl.java
package com.buddharoad.service.Member;

import com.buddharoad.dto.Member.LoginRequestDTO;
import com.buddharoad.dto.Member.LoginResponseDTO;
import com.buddharoad.dto.Member.MemberRequestDTO;
import com.buddharoad.dto.Member.MemberResponseDTO;
import com.buddharoad.dto.Member.MemberSearchFilterDTO; // ⭐ MemberSearchFilterDTO 임포트
import com.buddharoad.domain.Member;
import com.buddharoad.repository.Member.MemberRepository;
import com.buddharoad.security.JwtTokenProvider;
import com.buddharoad.security.Role;
import com.buddharoad.dto.Member.MemberUpdateDTO;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate; // ⭐ Predicate 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // ⭐ Page 임포트
import org.springframework.data.domain.PageRequest; // ⭐ PageRequest 임포트
import org.springframework.data.domain.Pageable; // ⭐ Pageable 임포트
import org.springframework.data.domain.Sort; // ⭐ Sort 임포트
import org.springframework.data.jpa.domain.Specification; // ⭐ Specification 임포트
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // ArrayList 임포트
import java.util.List; // List 임포트

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public MemberResponseDTO signUp(MemberRequestDTO memberRequestDto) {
        if (memberRepository.existsByLoginId(memberRequestDto.getLoginId())) {
            throw new EntityExistsException("이미 존재하는 아이디(Login ID)입니다.");
        }
        if (memberRepository.existsByEmail(memberRequestDto.getEmail())) {
            throw new EntityExistsException("이미 사용 중인 이메일입니다.");
        }
        if (memberRepository.existsByUsername(memberRequestDto.getUsername())) {
            throw new EntityExistsException("이미 존재하는 닉네임입니다.");
        }

        String encodedPassword = passwordEncoder.encode(memberRequestDto.getPassword());

        Member member = Member.builder()
                .loginId(memberRequestDto.getLoginId())
                .username(memberRequestDto.getUsername())
                .password(encodedPassword)
                .email(memberRequestDto.getEmail())
                .role(Role.USER)
                .build();

        Member savedMember = memberRepository.save(member);
        return MemberResponseDTO.fromEntity(savedMember);
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequestDto) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getLoginId(), loginRequestDto.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.createToken(authentication);

        Member member = memberRepository.findByLoginId(loginRequestDto.getLoginId())
                .orElseThrow(() -> new EntityNotFoundException("로그인 ID에 해당하는 사용자를 찾을 수 없습니다."));

        return new LoginResponseDTO(accessToken, member.getLoginId(), member.getUsername(), "로그인 성공!");
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponseDTO getMyInfo(String loginId) {
        System.out.println("DEBUG: getMyInfo - Attempting to find user with loginId: [" + loginId + "]");
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + loginId));

        return MemberResponseDTO.fromEntity(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponseDTO getMemberByLoginId(String loginId) {
        System.out.println("DEBUG: getMemberByLoginId - Attempting to find user with loginId: [" + loginId + "]");
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("로그인 ID에 해당하는 회원을 찾을 수 없습니다: " + loginId));
        return MemberResponseDTO.fromEntity(member);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    // ⭐ 수정: 회원 정보 수정 (email, username, password)
    @Transactional
    public void updateMemberInfo(String loginId, MemberUpdateDTO updateDTO) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + loginId));

        // 1. 이메일 업데이트 (null이 아니고, 기존 이메일과 다를 경우에만)
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(member.getEmail())) {
            if (memberRepository.existsByEmailAndMemberNoNot(updateDTO.getEmail(), member.getMemberNo())) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            member.updateEmail(updateDTO.getEmail());
        }

        // 2. 닉네임 업데이트 (null이 아니고, 기존 닉네임과 다를 경우에만)
        if (updateDTO.getUsername() != null && !updateDTO.getUsername().equals(member.getUsername())) {
            if (memberRepository.existsByUsernameAndMemberNoNot(updateDTO.getUsername(), member.getMemberNo())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            member.updateUsername(updateDTO.getUsername());
        }

        // 3. 비밀번호 업데이트 (기존 비밀번호와 새 비밀번호가 모두 제공되었을 경우)
        if (updateDTO.getOldPassword() != null && !updateDTO.getOldPassword().isEmpty() &&
                updateDTO.getNewPassword() != null && !updateDTO.getNewPassword().isEmpty()) {

            // ⭐ 기존 비밀번호 일치 여부 확인 시 명확한 에러 메시지
            if (!passwordEncoder.matches(updateDTO.getOldPassword(), member.getPassword())) {
                throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
            }
            // 새 비밀번호 유효성 검사는 DTO의 @Pattern으로 1차적으로 걸러짐.
            // 추가적인 비즈니스 로직 검사가 필요하면 여기에 추가 (예: 새 비밀번호가 기존과 같지 않아야 함)

            member.updatePassword(passwordEncoder.encode(updateDTO.getNewPassword()));
        } else if ((updateDTO.getOldPassword() != null && !updateDTO.getOldPassword().isEmpty()) ||
                (updateDTO.getNewPassword() != null && !updateDTO.getNewPassword().isEmpty())) {
            // 둘 중 하나만 입력된 경우 (보안을 위해 둘 다 입력 요구)
            throw new IllegalArgumentException("비밀번호 변경 시에는 기존 비밀번호와 새 비밀번호를 모두 입력해야 합니다.");
        }
        // Member 엔티티의 변경 감지(Dirty Checking) 덕분에 save() 호출 없이도 업데이트됨
    }

    // ⭐ 추가: 회원 탈퇴
    @Transactional
    public void withdrawMember(String loginId, String password) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + loginId));

        // ⭐ 비밀번호 확인 시 명확한 에러 메시지
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다. 회원 탈퇴에 실패했습니다.");
        }

        // 계정 상태를 'DEACTIVATED'로 변경 (실제 삭제 대신 비활성화)
        member.deactivateAccount();
        // memberRepository.save(member); // 변경 감지(Dirty Checking)로 자동 저장
    }

    // ⭐ 추가: 이메일 중복 확인 (회원 정보 수정 시 본인 제외)
    // 이 메서드는 프론트의 개별 중복 확인 버튼에서만 사용.
    // updateMemberInfo 에서는 existsByEmailAndMemberNoNot 사용
    public boolean checkEmailDuplicationForUpdate(String email, String currentLoginId) {
        Member currentMember = memberRepository.findByLoginId(currentLoginId)
                .orElseThrow(() -> new EntityNotFoundException("현재 로그인된 회원을 찾을 수 없습니다."));
        // 현재 이메일과 동일하면 중복 아님
        if (currentMember.getEmail().equals(email)) {
            return false;
        }
        return memberRepository.existsByEmail(email);
    }

    // ⭐ 추가: 닉네임 중복 확인 (회원 정보 수정 시 본인 제외)
    public boolean checkUsernameDuplicationForUpdate(String username, String currentLoginId) {
        Member currentMember = memberRepository.findByLoginId(currentLoginId)
                .orElseThrow(() -> new EntityNotFoundException("현재 로그인된 회원을 찾을 수 없습니다."));
        // 현재 닉네임과 동일하면 중복 아님
        if (currentMember.getDisplayName().equals(username)) { // getDisplayName()으로 닉네임 비교
            return false;
        }
        return memberRepository.existsByUsername(username);
    }

    // ⭐⭐ 추가: loginId로 Member 엔티티 자체를 가져오는 메서드
    @Override
    @Transactional(readOnly = true)
    public Member getMemberEntityByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + loginId));
    }

    // ⭐⭐ 추가: 관리자용 회원 목록 조회 및 검색
    @Override
    @Transactional(readOnly = true)
    public Page<MemberResponseDTO> getAllMembers(MemberSearchFilterDTO filterDTO) {
        Sort sort = Sort.by(filterDTO.getSortDirection(), filterDTO.getSortBy());
        Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize(), sort);

        Specification<Member> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 검색어 필터링
            if (filterDTO.getKeyword() != null && !filterDTO.getKeyword().isBlank()) {
                String keyword = "%" + filterDTO.getKeyword().toLowerCase() + "%";
                if ("LOGIN_ID".equalsIgnoreCase(filterDTO.getSearchType())) {
                    predicates.add(cb.like(cb.lower(root.get("loginId")), keyword));
                } else if ("USERNAME".equalsIgnoreCase(filterDTO.getSearchType())) {
                    predicates.add(cb.like(cb.lower(root.get("username")), keyword));
                } else if ("EMAIL".equalsIgnoreCase(filterDTO.getSearchType())) {
                    predicates.add(cb.like(cb.lower(root.get("email")), keyword));
                }
                // searchType이 없으면 모든 필드에서 검색 (선택 사항)
                // else {
                //     Predicate loginIdPredicate = cb.like(cb.lower(root.get("loginId")), keyword);
                //     Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), keyword);
                //     Predicate emailPredicate = cb.like(cb.lower(root.get("email")), keyword);
                //     predicates.add(cb.or(loginIdPredicate, usernamePredicate, emailPredicate));
                // }
            }

            // 계정 상태 필터링
            if (filterDTO.getAccountStatus() != null && !filterDTO.getAccountStatus().isBlank()) {
                predicates.add(cb.equal(root.get("accountStatus"), filterDTO.getAccountStatus()));
            }

            // 역할 필터링
            if (filterDTO.getRole() != null && !filterDTO.getRole().isBlank()) {
                predicates.add(cb.equal(root.get("role"), Role.valueOf(filterDTO.getRole()))); // 문자열을 Role Enum으로 변환
            }

            // 삭제되지 않은 회원만 조회 (기본값) -> accountStatus가 'DEACTIVATED'가 아닌 회원
            predicates.add(cb.notEqual(root.get("accountStatus"), "DEACTIVATED"));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Member> membersPage = memberRepository.findAll(spec, pageable);
        return membersPage.map(MemberResponseDTO::fromEntity);
    }

    // ⭐⭐ 추가: 관리자용 회원 상태 변경 (활성/정지/차단)
    @Override
    @Transactional
    public void updateMemberStatus(Long memberNo, String newStatus, Role currentAdminRole) {
        // ⭐ 관리자 권한 확인: SYSTEM_ADMIN만 변경 가능하도록
        if (currentAdminRole != Role.SYSTEM_ADMIN) {
            throw new IllegalArgumentException("회원 상태를 변경할 권한이 없습니다. (SYSTEM_ADMIN 권한 필요)");
        }

        Member member = memberRepository.findByMemberNo(memberNo)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberNo));

        // 유효한 상태 값인지 확인 (ACTIVE, BLOCKED, DEACTIVATED 등)
        if (!("ACTIVE".equals(newStatus) || "BLOCKED".equals(newStatus) || "DEACTIVATED".equals(newStatus))) {
            throw new IllegalArgumentException("유효하지 않은 계정 상태 값입니다: " + newStatus);
        }

        member.updateAccountStatus(newStatus); // Member 엔티티의 updateAccountStatus 메서드 사용
        // @Transactional이 자동 저장
    }

    // ⭐⭐ 추가: 관리자용 회원 소프트 삭제 (accountStatus를 DEACTIVATED로 변경)
    @Override
    @Transactional
    public void adminDeleteMember(Long memberNo, Role currentAdminRole) {
        // ⭐ 관리자 권한 확인: SYSTEM_ADMIN만 삭제 가능하도록
        if (currentAdminRole != Role.SYSTEM_ADMIN) {
            throw new IllegalArgumentException("회원을 삭제할 권한이 없습니다. (SYSTEM_ADMIN 권한 필요)");
        }

        Member member = memberRepository.findByMemberNo(memberNo)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberNo));

        // 이미 탈퇴된 상태라면
        if ("DEACTIVATED".equals(member.getAccountStatus())) {
            throw new IllegalArgumentException("이미 탈퇴 처리된 회원입니다.");
        }

        member.deactivateAccount(); // Member 엔티티의 deactivateAccount 메서드 사용 (DEACTIVATED로 변경)
        // @Transactional이 자동 저장
    }

    // ⭐⭐ getMemberRole() 메서드를 추가합니다.
    @Override
    @Transactional(readOnly = true)
    public Role getMemberRole(Long memberNo) {
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberNo));
        return member.getRole();
    }
}