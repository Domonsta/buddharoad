// src/main/java/com/buddharoad/controller/TempleCommentController.java
package com.buddharoad.controller; // 적절한 패키지 경로로 수정해주세요.

import com.buddharoad.dto.Temples.Comment.TempleCommentRegisterRequestDTO;
import com.buddharoad.dto.Temples.Comment.TempleCommentResponseDTO;
import com.buddharoad.dto.Temples.Comment.TempleCommentSearchFilterDTO;
import com.buddharoad.dto.Temples.Comment.TempleCommentUpdateRequestDTO;
import com.buddharoad.security.Role; // Role Enum 임포트
import com.buddharoad.service.Temples.TempleCommentService;
import com.buddharoad.service.Member.MemberService; // 💡 MemberService 임포트
import com.buddharoad.dto.Member.MemberResponseDTO; // 💡 MemberResponseDTO 임포트
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 권한 부여를 위해 추가
import org.springframework.security.core.Authentication; // Authentication 임포트
import org.springframework.security.core.userdetails.UserDetails; // UserDetails 임포트
import org.springframework.validation.BindingResult; // 유효성 검사 결과
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // ResponseStatusException 임포트 (이전 버전에서 누락된 경우 추가)

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/temples/{templeId}/comments") // 특정 사찰에 종속적인 댓글 API 경로
@RequiredArgsConstructor
@Log4j2
public class TempleCommentController {

    private final TempleCommentService templeCommentService;
    private final MemberService memberService; // MemberService 주입

    // 💡 댓글 등록 (POST)
    // - 권한: 일반 회원(USER), 콘텐츠 관리자(CONTENT_ADMIN), 시스템 관리자(SYSTEM_ADMIN)
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> registerComment(
            @PathVariable Long templeId, // URL Path에서 사찰 ID를 받음
            @Valid @RequestBody TempleCommentRegisterRequestDTO requestDTO, // JSON 요청 본문으로 댓글 내용 DTO를 받음
            BindingResult bindingResult,
            Authentication authentication // 현재 인증된 사용자 정보 주입
    ) {
        log.info("📢 댓글 등록 요청: 사찰 ID {}, 내용: {}", templeId, requestDTO.getContent());

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.warn("🚨 댓글 등록 유효성 검사 실패: {}", errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }

        try {
            // DTO에 사찰 ID 설정 (URL PathVariable과 일치시킴)
            requestDTO.setTempleId(templeId);

            // 현재 로그인한 사용자의 회원 번호와 역할 가져오기
            Long memberNo = null;
            Role currentUserRole = Role.GUEST; // 기본값은 GUEST (비회원)
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                String usernameFromAuth = authentication.getName(); // UserDetails.getUsername()은 닉네임일 가능성 높음

                // 💡💡💡 수정: memberService.getMemberByLoginId(loginId) 대신 memberService.getMyInfo(username) 사용 💡💡💡
                // authentication.getName()이 닉네임을 반환한다는 가정 하에, 닉네임으로 회원 정보 조회
                MemberResponseDTO memberInfo = memberService.getMyInfo(usernameFromAuth);
                memberNo = memberInfo.getMemberNo();
                log.info("인증된 사용자 닉네임(from Auth): {}, memberNo: {}", usernameFromAuth, memberNo);

                // 역할 정보 가져오기 (Authentication 객체에서 권한 목록을 가져와 Role Enum으로 변환)
                String roleString = authentication.getAuthorities().stream()
                        .findFirst()
                        .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", "")) // "ROLE_USER" -> "USER"
                        .orElse("GUEST");
                try {
                    currentUserRole = Role.valueOf(roleString);
                } catch (IllegalArgumentException e) {
                    log.warn("알 수 없는 역할: {}", roleString);
                    currentUserRole = Role.GUEST; // 기본값 GUEST
                }
            } else {
                log.warn("🚨 댓글 등록 요청 실패: 인증되지 않은 사용자 또는 권한 정보 없음.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
            }

            // 💡 requestDTO에 memberNo 설정 (프론트에서 memberNo를 보내지 않는 경우, 백엔드에서 주입)
            // TempleCommentRegisterRequestDTO에 memberNo 필드가 있다면 설정, 없다면 이 줄 제거
            // 현재 DTO에는 memberNo 필드가 없어야 합니다.
            // requestDTO.setMemberNo(memberNo); // 이 줄은 TempleCommentRegisterRequestDTO에 memberNo 필드가 없을 경우 제거

            TempleCommentResponseDTO registeredComment = templeCommentService.registerComment(requestDTO, memberNo, currentUserRole);
            log.info("✅ 댓글 등록 성공: 댓글 ID {}", registeredComment.getTempleCommentId());
            return new ResponseEntity<>(registeredComment, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 댓글 등록 실패 (권한 부족 또는 잘못된 요청): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403 Forbidden
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 댓글 등록 실패 (사찰 또는 회원 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (Exception e) {
            log.error("❌ 댓글 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 특정 사찰의 댓글 목록 검색 및 페이징 (GET)
    // - 권한: 모든 사용자 접근 가능
    @GetMapping
    public ResponseEntity<?> searchComments(
            @PathVariable Long templeId,
            @ModelAttribute TempleCommentSearchFilterDTO filterDTO, // 쿼리 파라미터를 DTO에 바인딩
            Authentication authentication // 💡 Authentication 객체 추가
    ) {
        log.info("🔍 댓글 목록 검색 요청: 사찰 ID {}, 필터: {}", templeId, filterDTO);
        try {
            // 로그인한 사용자의 ID (username) 가져오기
            String userId = authentication != null ? authentication.getName() : "anonymousUser"; // 비회원 처리
            log.info("댓글 목록 조회 요청 사용자 ID: {}", userId);

            // 💡💡💡 수정: userId 인자 추가 💡💡💡
            Page<TempleCommentResponseDTO> pageResult = templeCommentService.searchComments(templeId, filterDTO, userId);
            log.info("✅ 댓글 목록 검색 성공. 총 {}개, 현재 페이지 {}/{}",
                    pageResult.getTotalElements(), pageResult.getNumber() + 1, pageResult.getTotalPages());
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("❌ 댓글 목록 검색 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 목록 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 특정 댓글 상세 조회 (GET)
    // - 권한: 모든 사용자 접근 가능
    @GetMapping("/{commentId}")
    public ResponseEntity<?> getCommentDetails(
            @PathVariable Long templeId, // 사찰 ID는 경로 일치를 위해 받지만, 실제 사용은 commentId로
            @PathVariable Long commentId,
            Authentication authentication // 현재 인증된 사용자 정보 주입
    ) {
        log.info("🔎 댓글 상세 조회 요청: 사찰 ID {}, 댓글 ID {}", templeId, commentId);
        try {
            // 로그인한 사용자의 ID (username) 가져오기
            String userId = authentication != null ? authentication.getName() : "anonymousUser"; // 비회원 처리
            log.info("댓글 조회 요청 사용자 ID: {}", userId);

            TempleCommentResponseDTO commentDetails = templeCommentService.getCommentDetails(commentId, userId);
            log.info("✅ 댓글 상세 조회 성공: 댓글 ID {}", commentDetails.getTempleCommentId());
            return ResponseEntity.ok(commentDetails);
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 댓글을 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 댓글 상세 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 상세 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 댓글 수정 (PUT)
    // - 권한: 일반 회원(USER), 콘텐츠 관리자(CONTENT_ADMIN), 시스템 관리자(SYSTEM_ADMIN)
    @PutMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> updateComment(
            @PathVariable Long templeId, // 사찰 ID는 경로 일치를 위해 받지만, 실제 사용은 commentId로
            @PathVariable Long commentId,
            @Valid @RequestBody TempleCommentUpdateRequestDTO requestDTO,
            BindingResult bindingResult,
            Authentication authentication
    ) {
        log.info("🔄 댓글 수정 요청: 사찰 ID {}, 댓글 ID {}", templeId, commentId);

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.warn("🚨 댓글 수정 유효성 검사 실패: {}", errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }

        try {
            // DTO에 댓글 ID와 사찰 ID 설정 (URL PathVariable과 일치시킴)
            requestDTO.setTempleCommentId(commentId);
            requestDTO.setTempleId(templeId);

            // 현재 로그인한 사용자의 회원 번호와 역할 가져오기
            Long memberNo = null;
            Role currentUserRole = Role.GUEST;
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                String usernameFromAuth = authentication.getName(); // UserDetails.getUsername()은 닉네임일 가능성 높음
                // 💡💡💡 수정: memberService.getMemberByLoginId(loginId) 대신 memberService.getMyInfo(username) 사용 💡💡💡
                MemberResponseDTO memberInfo = memberService.getMyInfo(usernameFromAuth);
                memberNo = memberInfo.getMemberNo();

                String roleString = authentication.getAuthorities().stream()
                        .findFirst()
                        .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                        .orElse("GUEST");
                try {
                    currentUserRole = Role.valueOf(roleString);
                } catch (IllegalArgumentException e) {
                    log.warn("알 수 없는 역할: {}", roleString);
                    currentUserRole = Role.GUEST;
                }
            } else {
                log.warn("🚨 댓글 수정 요청 실패: 인증되지 않은 사용자 또는 권한 정보 없음.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
            }

            TempleCommentResponseDTO updatedComment = templeCommentService.updateComment(requestDTO, memberNo, currentUserRole);
            log.info("✅ 댓글 수정 성공: 댓글 ID {}", updatedComment.getTempleCommentId());
            return ResponseEntity.ok(updatedComment);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 댓글 수정 실패 (권한 부족 또는 잘못된 요청): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403 Forbidden
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 댓글 수정 실패 (댓글을 찾을 수 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (Exception e) {
            log.error("❌ 댓글 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 댓글 삭제 (DELETE)
    // - 권한: 일반 회원(USER), 콘텐츠 관리자(CONTENT_ADMIN), 시스템 관리자(SYSTEM_ADMIN)
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long templeId, // 사찰 ID는 경로 일치를 위해 받지만, 실제 사용은 commentId로
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        log.info("🗑️ 댓글 삭제 요청: 사찰 ID {}, 댓글 ID {}", templeId, commentId);

        try {
            // 현재 로그인한 사용자의 회원 번호와 역할 가져오기
            Long memberNo = null;
            Role currentUserRole = Role.GUEST;
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                String usernameFromAuth = authentication.getName(); // UserDetails.getUsername()은 닉네임일 가능성 높음
                // 💡💡💡 수정: memberService.getMemberByLoginId(loginId) 대신 memberService.getMyInfo(username) 사용 💡💡💡
                MemberResponseDTO memberInfo = memberService.getMyInfo(usernameFromAuth);
                memberNo = memberInfo.getMemberNo();

                String roleString = authentication.getAuthorities().stream()
                        .findFirst()
                        .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                        .orElse("GUEST");
                try {
                    currentUserRole = Role.valueOf(roleString);
                } catch (IllegalArgumentException e) {
                    log.warn("알 수 없는 역할: {}", roleString);
                    currentUserRole = Role.GUEST;
                }
            } else {
                log.warn("🚨 댓글 삭제 요청 실패: 인증되지 않은 사용자 또는 권한 정보 없음.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
            }

            templeCommentService.deleteComment(commentId, memberNo, currentUserRole);
            log.info("✅ 댓글 삭제 성공: 댓글 ID {}", commentId);
            return ResponseEntity.ok("댓글이 성공적으로 삭제(비활성화)되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 댓글 삭제 실패 (권한 부족 또는 잘못된 요청): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403 Forbidden
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 댓글 삭제 실패 (댓글을 찾을 수 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (Exception e) {
            log.error("❌ 댓글 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}