package com.buddharoad.controller;

import com.buddharoad.dto.Member.*;
import com.buddharoad.security.Role;
import com.buddharoad.service.Member.MemberService;
import com.buddharoad.service.Reviews.ReviewService;
import com.buddharoad.service.Reviews.ReviewCommentService;
import com.buddharoad.dto.Reviews.Review.ReviewResponseDTO;
import com.buddharoad.dto.Reviews.Review.ReviewSearchFilterDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentResponseDTO;
import com.buddharoad.service.Temples.TempleCommentService; // ⭐ TempleCommentService 임포트
import com.buddharoad.dto.Temples.Comment.TempleCommentResponseDTO; // ⭐ TempleCommentResponseDTO 임포트
import com.buddharoad.dto.Admin.AdminResponseDTO; // ⭐ AdminResponseDTO 임포트
import com.buddharoad.dto.Admin.AdminUpdateDTO; // ⭐ AdminUpdateDTO 임포트
import com.buddharoad.domain.Member; // Member 엔티티 임포트 (getMemberEntityByLoginId 사용 위함)

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2; // Log4j2 임포트
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth") // 기존 인증 관련 API 경로
@RequiredArgsConstructor
@Log4j2 // Log4j2 어노테이션
public class MemberController {

    private final MemberService memberService;
    private final ReviewService reviewService;
    private final ReviewCommentService reviewCommentService;
    private final TempleCommentService templeCommentService; // ⭐ TempleCommentService 필드 주입

    // 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<MemberResponseDTO> signUp(@Valid @RequestBody MemberRequestDTO memberRequestDto) {
        MemberResponseDTO memberResponseDto = memberService.signUp(memberRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberResponseDto);
    }

    // 아이디(loginId) 중복 확인 API 추가
    @GetMapping("/check-login-id")
    public ResponseEntity<Boolean> checkLoginIdDuplicate(@RequestParam String loginId) {
        boolean isDuplicated = memberService.existsByLoginId(loginId);
        return ResponseEntity.ok(isDuplicated);
    }

    // 이메일 중복 확인 API 추가 (회원가입용)
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicated = memberService.existsByEmail(email);
        return ResponseEntity.ok(isDuplicated);
    }

    // 닉네임(username) 중복 확인 API 추가 (회원가입용)
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameDuplicate(@RequestParam String username) {
        boolean isDuplicated = memberService.existsByUsername(username);
        return ResponseEntity.ok(isDuplicated);
    }

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDto) {
        LoginResponseDTO loginResponseDto = memberService.login(loginRequestDto);
        return ResponseEntity.ok(loginResponseDto);
    }

    // --- 마이페이지 관련 API ---

    @GetMapping("/me") // 내 정보 보기
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')") // 일반회원 이상 접근 가능
    public ResponseEntity<MemberResponseDTO> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String loginId = userDetails.getUsername(); // loginId를 가져옴
        System.out.println("DEBUG: MemberController - Request for my info with loginId: [" + loginId + "]");
        MemberResponseDTO memberInfo = memberService.getMyInfo(loginId);
        return ResponseEntity.ok(memberInfo);
    }

    // ⭐ 추가: 회원 정보 수정
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')") // 일반회원 이상 접근 가능
    public ResponseEntity<String> updateMyInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MemberUpdateDTO updateDTO) { // @Valid로 DTO 유효성 검사
        String loginId = userDetails.getUsername();
        memberService.updateMemberInfo(loginId, updateDTO);
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
    }

    // ⭐ 추가: 회원 정보 수정 시 이메일 중복 확인 (본인 제외)
    @GetMapping("/me/check-email-for-update")
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<Boolean> checkEmailDuplicateForUpdate(
            @RequestParam String email,
            @AuthenticationPrincipal UserDetails userDetails) {
        String currentLoginId = userDetails.getUsername();
        boolean isDuplicated = memberService.checkEmailDuplicationForUpdate(email, currentLoginId);
        return ResponseEntity.ok(isDuplicated);
    }

    // ⭐ 추가: 회원 정보 수정 시 닉네임 중복 확인 (본인 제외)
    @GetMapping("/me/check-username-for-update")
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<Boolean> checkUsernameDuplicateForUpdate(
            @RequestParam String username,
            @AuthenticationPrincipal UserDetails userDetails) {
        String currentLoginId = userDetails.getUsername();
        boolean isDuplicated = memberService.checkUsernameDuplicationForUpdate(username, currentLoginId);
        return ResponseEntity.ok(isDuplicated);
    }

    // ⭐ 추가: 회원 탈퇴
    @DeleteMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')") // 일반회원 이상 접근 가능
    public ResponseEntity<String> withdrawMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MemberWithdrawalDTO withdrawalDTO) { // @Valid로 비밀번호 필수 검사
        String loginId = userDetails.getUsername();
        memberService.withdrawMember(loginId, withdrawalDTO.getPassword());
        return ResponseEntity.ok("회원 탈퇴가 성공적으로 처리되었습니다.");
    }

    // --- ⭐⭐ 마이페이지 '여정 나누기' 탭 관련 API ⭐⭐ ---

    /**
     * 내가 쓴 리뷰 목록 조회 (마이페이지 - 여정 나누기 탭)
     * GET /api/auth/me/reviews
     * @param userDetails 현재 인증된 사용자 정보
     * @param filterDTO 검색 필터 및 페이징 정보 (searchKeyword, sortBy, sortOrder, page, size)
     * @return 페이징 처리된 리뷰 목록 또는 오류 메시지
     */
    @GetMapping("/me/reviews")
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute ReviewSearchFilterDTO filterDTO) {
        String loginId = userDetails.getUsername(); // 현재 로그인된 사용자의 loginId
        log.info("🔎 내 리뷰 목록 조회 요청: 요청자: {}, 필터: {}", loginId, filterDTO);

        try {
            // memberNo를 가져와 서비스에 전달
            Long memberNo = memberService.getMemberByLoginId(loginId).getMemberNo();
            Page<ReviewResponseDTO> myReviewsPage = reviewService.getMyReviews(memberNo, filterDTO);
            log.info("✅ 내 리뷰 목록 조회 성공: 총 {}개 리뷰, {} 페이지", myReviewsPage.getTotalElements(), myReviewsPage.getNumber());
            return ResponseEntity.ok(myReviewsPage);
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 내 리뷰 목록 조회 실패 (회원을 찾을 수 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 내 리뷰 목록 조회 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 내 리뷰 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("내 리뷰 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 내가 작성한 댓글 목록 조회 (마이페이지 - 여정 나누기 탭)
     * GET /api/auth/me/comments
     * boardType에 따라 리뷰 댓글 또는 사찰 댓글 조회
     * @param userDetails 현재 인증된 사용자 정보
     * @param boardType 검색할 게시판 타입 ("REVIEW" 또는 "TEMPLE")
     * @param keyword 검색 키워드 (댓글 내용 또는 원본 게시글 제목)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortBy 정렬 기준 (createdAt, viewCount 등)
     * @param sortOrder 정렬 순서 (asc, desc)
     * @return 페이징 처리된 댓글 목록 또는 오류 메시지
     */
    @GetMapping("/me/comments")
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> getMyComments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "REVIEW") String boardType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        String loginId = userDetails.getUsername(); // 현재 로그인된 사용자의 loginId
        log.info("🔎 내 댓글 목록 조회 요청: 요청자: {}, 게시판 타입: {}, 검색어: {}", loginId, boardType, keyword);

        try {
            Long memberNo = memberService.getMemberByLoginId(loginId).getMemberNo();

            if ("REVIEW".equalsIgnoreCase(boardType)) {
                Page<ReviewCommentResponseDTO> myReviewCommentsPage =
                        reviewCommentService.getMyReviewComments(memberNo, keyword, page, size, sortBy, sortOrder);
                log.info("✅ 내 리뷰 댓글 목록 조회 성공: 총 {}개 댓글, {} 페이지", myReviewCommentsPage.getTotalElements(), myReviewCommentsPage.getNumber());
                return ResponseEntity.ok(myReviewCommentsPage);
            } else if ("TEMPLE".equalsIgnoreCase(boardType)) {
                Page<TempleCommentResponseDTO> myTempleCommentsPage =
                        templeCommentService.getMyTempleComments(memberNo, keyword, page, size, sortBy, sortOrder);
                log.info("✅ 내 사찰 댓글 목록 조회 성공: 총 {}개 댓글, {} 페이지", myTempleCommentsPage.getTotalElements(), myTempleCommentsPage.getNumber());
                return ResponseEntity.ok(myTempleCommentsPage);
            } else {
                log.warn("⚠️ 지원하지 않는 게시판 타입입니다: {}", boardType);
                return ResponseEntity.badRequest().body("지원하지 않는 게시판 타입입니다. (REVIEW, TEMPLE만 가능)");
            }
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 내 댓글 목록 조회 실패 (회원을 찾을 수 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 내 댓글 목록 조회 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 내 댓글 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("내 댓글 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 관리자 계정 정보 조회 (관리자 페이지 - 계정 정보 탭)
     * GET /api/auth/admin/me
     * @param userDetails 현재 인증된 관리자 정보
     * @return 관리자 정보 DTO
     */
    @GetMapping("/admin/me")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'CONTENT_ADMIN')") // ⭐ 관리자 권한만 접근 가능
    public ResponseEntity<AdminResponseDTO> getAdminInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String loginId = userDetails.getUsername(); // 관리자의 loginId
        log.info("🔎 관리자 계정 정보 조회 요청: 요청 관리자 Login ID: [{}]", loginId);
        try {
            // memberService.getMemberByLoginId(loginId)는 MemberResponseDTO를 반환
            // AdminResponseDTO.fromEntity()는 Member 엔티티를 기대
            // 따라서 Member 엔티티를 반환하는 새로운 서비스 메서드 필요 (Member getMemberEntityByLoginId(String loginId))
            // 또는 AdminResponseDTO.fromMemberResponseDTO() 와 같은 DTO-to-DTO 변환 메서드 필요
            AdminResponseDTO adminInfo = AdminResponseDTO.fromEntity(
                    memberService.getMemberEntityByLoginId(loginId) // Member 엔티티를 반환하는 메서드 호출
            );
            log.info("✅ 관리자 계정 정보 조회 성공: 관리자 닉네임: {}", adminInfo.getUsername());
            return ResponseEntity.ok(adminInfo);
        } catch (EntityNotFoundException e) {
            log.error("❌ 관리자 계정을 찾을 수 없음: {}", loginId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 에러 응답
        } catch (Exception e) {
            log.error("❌ 관리자 계정 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 관리자 계정 정보 수정 (관리자 페이지 - 계정 정보 탭)
     * PUT /api/auth/admin/me
     * @param userDetails 현재 인증된 관리자 정보
     * @param updateDTO 수정 요청 DTO (이메일, 닉네임, 비밀번호)
     * @return 성공/실패 메시지
     */
    @PutMapping("/admin/me")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'CONTENT_ADMIN')") // ⭐ 관리자 권한만 접근 가능
    public ResponseEntity<String> updateAdminInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AdminUpdateDTO updateDTO) { // @Valid로 DTO 유효성 검사
        String loginId = userDetails.getUsername(); // 관리자의 loginId
        log.info("🔄 관리자 계정 정보 수정 요청: 요청 관리자 Login ID: [{}]", loginId);
        try {
            // memberService의 updateMemberInfo를 재활용하여 관리자 정보 수정
            // AdminUpdateDTO는 MemberUpdateDTO와 유사하므로, MemberUpdateDTO로 변환하거나
            // MemberService의 updateMemberInfo가 AdminUpdateDTO를 받도록 오버로드할 수 있음
            // 여기서는 AdminUpdateDTO의 필드들을 MemberUpdateDTO에 매핑하여 호출
            memberService.updateMemberInfo(loginId, new com.buddharoad.dto.Member.MemberUpdateDTO(
                    updateDTO.getEmail(),
                    updateDTO.getUsername(), // AdminUpdateDTO의 username -> MemberUpdateDTO의 nickname
                    updateDTO.getOldPassword(),
                    updateDTO.getNewPassword()
            ));
            log.info("✅ 관리자 계정 정보 성공적으로 수정됨: Login ID: {}", loginId);
            return ResponseEntity.ok("관리자 계정 정보가 성공적으로 수정되었습니다.");
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 관리자 계정 수정 실패 (관리자를 찾을 수 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 관리자 계정 수정 실패 (잘못된 요청 또는 권한 부족): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 관리자 계정 정보 수정 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("관리자 계정 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ⭐ 참고: 관리자 계정의 이메일/닉네임 중복 확인도 필요하다면,
    // 기존 /api/auth/me/check-email-for-update, /api/auth/me/check-username-for-update를 재활용하거나
    // /api/auth/admin/check-email, /api/auth/admin/check-username 같은 새 엔드포인트를 만들어 재활용할 수 있음.
    // 여기서는 기존 API를 사용한다고 가정합니다.

    // --- ⭐⭐ 관리자 페이지 '회원 관리 탭' 관련 API 추가 ⭐⭐ ---

    /**
     * 관리자: 전체 회원 목록 조회 및 검색
     * GET /api/auth/admin/members
     * @param filterDTO 검색 필터 및 페이징 정보 (searchType, keyword, accountStatus, role, sortBy, sortOrder, page, size)
     * @return 페이징 처리된 회원 목록 (MemberResponseDTO)
     */
    @GetMapping("/admin/members")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'CONTENT_ADMIN')") // ⭐ 관리자 권한 필요
    public ResponseEntity<?> getAllMembers(
            @ModelAttribute MemberSearchFilterDTO filterDTO,
            @AuthenticationPrincipal UserDetails userDetails) { // 요청 관리자 로그용
        log.info("🔎 관리자: 전체 회원 목록 조회 요청. 요청 관리자: {}, 필터: {}", userDetails.getUsername(), filterDTO);
        try {
            Page<MemberResponseDTO> membersPage = memberService.getAllMembers(filterDTO);
            log.info("✅ 관리자: 전체 회원 목록 조회 성공: 총 {}명 회원, {} 페이지", membersPage.getTotalElements(), membersPage.getNumber());
            return ResponseEntity.ok(membersPage);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 관리자: 회원 목록 조회 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 관리자: 회원 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 관리자: 특정 회원의 계정 상태 변경 (활성/정지/차단)
     * PATCH /api/auth/admin/members/{memberNo}/status
     * @param memberNo 변경할 회원의 고유 번호
     * @param newStatus 새로운 계정 상태 (ACTIVE, BLOCKED, DEACTIVATED)
     * @param userDetails 현재 인증된 관리자 정보
     * @return 성공 메시지
     */
    @PatchMapping("/admin/members/{memberNo}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')") // ⭐ SYSTEM_ADMIN만 허용 (민감한 작업)
    public ResponseEntity<String> updateMemberStatus(
            @PathVariable Long memberNo,
            @RequestParam String newStatus,
            @AuthenticationPrincipal UserDetails userDetails) {
        String adminLoginId = userDetails.getUsername();
        log.info("🔄 관리자: 회원 상태 변경 요청. 변경 대상 회원 No: {}, 새 상태: {}, 요청 관리자: {}", memberNo, newStatus, adminLoginId);
        try {
            // 현재 로그인한 관리자의 권한을 가져와 서비스에 전달
            Member adminMember = memberService.getMemberEntityByLoginId(adminLoginId);
            Role currentAdminRole = adminMember.getRole();

            memberService.updateMemberStatus(memberNo, newStatus, currentAdminRole);
            log.info("✅ 관리자: 회원 상태 변경 성공. 회원 No: {}, 새 상태: {}", memberNo, newStatus);
            return ResponseEntity.ok("회원 계정 상태가 성공적으로 변경되었습니다.");
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 관리자: 회원 상태 변경 실패 (회원 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 관리자: 회원 상태 변경 실패 (권한 부족 또는 잘못된 요청): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403 Forbidden
        } catch (Exception e) {
            log.error("❌ 관리자: 회원 상태 변경 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 관리자: 특정 회원 탈퇴 처리 (소프트 삭제)
     * PATCH /api/auth/admin/members/{memberNo}/delete
     * @param memberNo 탈퇴 처리할 회원의 고유 번호
     * @param userDetails 현재 인증된 관리자 정보
     * @return 성공 메시지
     */
    @PatchMapping("/admin/members/{memberNo}/delete")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')") // ⭐ SYSTEM_ADMIN만 허용 (민감한 작업)
    public ResponseEntity<String> adminDeleteMember(
            @PathVariable Long memberNo,
            @AuthenticationPrincipal UserDetails userDetails) {
        String adminLoginId = userDetails.getUsername();
        log.info("🗑️ 관리자: 회원 탈퇴 처리 요청. 탈퇴 대상 회원 No: {}, 요청 관리자: {}", memberNo, adminLoginId);
        try {
            // 현재 로그인한 관리자의 권한을 가져와 서비스에 전달
            Member adminMember = memberService.getMemberEntityByLoginId(adminLoginId);
            Role currentAdminRole = adminMember.getRole();

            memberService.adminDeleteMember(memberNo, currentAdminRole);
            log.info("✅ 관리자: 회원 탈퇴 처리 성공. 회원 No: {}", memberNo);
            return ResponseEntity.ok("회원이 성공적으로 탈퇴 처리(비활성화)되었습니다.");
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 관리자: 회원 탈퇴 처리 실패 (회원 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 관리자: 회원 탈퇴 처리 실패 (권한 부족 또는 잘못된 요청): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403 Forbidden
        } catch (Exception e) {
            log.error("❌ 관리자: 회원 탈퇴 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 관리자 권한별 접근 제어 API 예시 (⭐⭐ 이 블록이 파일 내에 한 번만 존재해야 합니다! ⭐⭐)
    @GetMapping("/admin/system-status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<String> systemAdminTest() {
        return ResponseEntity.ok("시스템 관리자만 접근 가능한 페이지입니다.");
    }

    @GetMapping("/admin/content-moderation")
    @PreAuthorize("hasRole('CONTENT_ADMIN')")
    public ResponseEntity<String> contentAdminTest() {
        return ResponseEntity.ok("콘텐츠 관리자만 접근 가능한 페이지입니다.");
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'CONTENT_ADMIN')")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("모든 관리자가 접근 가능한 대시보드입니다.");
    }
}