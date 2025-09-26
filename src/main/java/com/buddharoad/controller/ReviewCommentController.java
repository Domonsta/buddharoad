// src/main/java/com/buddharoad/controller/ReviewCommentController.java
package com.buddharoad.controller;

import com.buddharoad.dto.Reviews.Comment.ReviewCommentRegisterRequestDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentResponseDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentSearchFilterDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentUpdateRequestDTO;
import com.buddharoad.security.Role;
import com.buddharoad.service.Reviews.ReviewCommentService;
import com.buddharoad.service.Member.MemberService;
import com.buddharoad.dto.Member.MemberResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews/{reviewId}/comments")
@RequiredArgsConstructor
@Log4j2
public class ReviewCommentController {

    private final ReviewCommentService reviewCommentService;
    private final MemberService memberService;

    // 댓글 등록 메서드는 기존과 동일하게 사용하면 돼!
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> registerComment(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewCommentRegisterRequestDTO requestDTO,
            BindingResult bindingResult,
            Authentication authentication) {
        log.info("📧 리뷰 댓글 등록 요청: 리뷰 ID {}, 요청자: {}", reviewId, authentication.getName());

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.warn("⚠️ 리뷰 댓글 등록 유효성 검사 실패: {}", errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }

        if (!reviewId.equals(requestDTO.getReviewId())) {
            log.warn("🚨 URL 리뷰 ID({})와 DTO 리뷰 ID({}) 불일치.", reviewId, requestDTO.getReviewId());
            return ResponseEntity.badRequest().body("요청된 리뷰 ID와 댓글의 리뷰 ID가 일치하지 않습니다.");
        }

        Long memberNo;
        String memberUsername;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            try {
                MemberResponseDTO memberInfo = memberService.getMemberByLoginId(userDetails.getUsername());
                memberNo = memberInfo.getMemberNo();
                memberUsername = memberInfo.getUsername();
            } catch (EntityNotFoundException e) {
                log.error("❌ 등록되지 않은 회원으로 댓글 등록 시도: {}", userDetails.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 사용자 정보입니다.");
            }
        } else {
            log.warn("🚨 리뷰 댓글 등록 요청 실패: 인증되지 않은 사용자.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        try {
            ReviewCommentResponseDTO responseDTO = reviewCommentService.registerComment(requestDTO, memberNo, memberUsername);
            // 💡 getReviewCommentId() 대신 복합 키 정보로 로그 출력
            log.info("✅ 리뷰 댓글 등록 성공: 리뷰 ID {}, 회원 번호 {}", responseDTO.getReviewId(), responseDTO.getMemberNo());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 리뷰 댓글 등록 실패 (리뷰 또는 회원 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 리뷰 댓글 등록 실패 (이미 댓글이 존재): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 리뷰 댓글 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 GET 메서드: 단일 댓글 조회. 복합 키를 사용하므로 URL 경로 변경!
    @GetMapping("/{memberNo}")
    public ResponseEntity<?> getComment(
            @PathVariable Long reviewId,
            @PathVariable Long memberNo) {
        log.info("🔍 특정 리뷰 댓글 조회 요청: 리뷰 ID {}, 회원 번호 {}", reviewId, memberNo);

        try {
            // 💡 조회수 관련 로직은 서비스에서 제거했으므로, getComment() 메서드만 호출
            ReviewCommentResponseDTO responseDTO = reviewCommentService.getComment(reviewId, memberNo);
            // 💡 DTO에서 viewCount 필드가 삭제되었으므로, 관련 로그도 제거
            log.info("✅ 리뷰 댓글 조회 성공: 리뷰 ID {}, 회원 번호 {}", responseDTO.getReviewId(), responseDTO.getMemberNo());
            return ResponseEntity.ok(responseDTO);
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 리뷰 댓글 조회 실패 (댓글을 찾을 수 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 리뷰 댓글 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // searchComments는 기존과 동일하게 사용!
    @GetMapping
    public ResponseEntity<?> searchComments(
            @PathVariable Long reviewId,
            @ModelAttribute ReviewCommentSearchFilterDTO filter) {
        log.info("🔎 리뷰 댓글 목록 검색 요청: 리뷰 ID {}, 필터: {}", reviewId, filter);

        filter.setReviewId(reviewId);

        try {
            Page<ReviewCommentResponseDTO> commentPage = reviewCommentService.searchComments(filter);
            log.info("✅ 리뷰 댓글 검색 성공: 총 {}개 댓글, {} 페이지", commentPage.getTotalElements(), commentPage.getNumber());
            return ResponseEntity.ok(commentPage);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 리뷰 댓글 검색 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 리뷰 댓글 검색 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 PUT 메서드: 댓글 업데이트. URL 경로와 로직 변경!
    @PutMapping("/{memberNo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateComment(
            @PathVariable Long reviewId,
            @PathVariable Long memberNo, // URL에서 memberNo를 받아옴
            @Valid @RequestBody ReviewCommentUpdateRequestDTO requestDTO,
            BindingResult bindingResult,
            Authentication authentication) {
        log.info("✏️ 리뷰 댓글 업데이트 요청: 리뷰 ID {}, 회원 번호 {}, 요청자: {}", reviewId, memberNo, authentication.getName());

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.warn("⚠️ 리뷰 댓글 업데이트 유효성 검사 실패: {}", errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }

        // 💡 DTO에 reviewCommentId가 없으므로 해당 검증 로직 제거!
        if (!reviewId.equals(requestDTO.getReviewId())) {
            log.warn("🚨 URL 리뷰 ID({})와 DTO 리뷰 ID({}) 불일치.", reviewId, requestDTO.getReviewId());
            return ResponseEntity.badRequest().body("요청된 ID 정보가 일치하지 않습니다.");
        }

        // 인증된 사용자의 정보 추출
        Long authenticatedMemberNo;
        Role currentUserRole;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            try {
                MemberResponseDTO memberInfo = memberService.getMemberByLoginId(userDetails.getUsername());
                authenticatedMemberNo = memberInfo.getMemberNo();
            } catch (EntityNotFoundException e) {
                log.error("❌ 등록되지 않은 회원으로 댓글 수정 시도: {}", userDetails.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 사용자 정보입니다.");
            }

            // URL의 memberNo와 인증된 사용자의 memberNo가 다르면 권한 오류
            if (!authenticatedMemberNo.equals(memberNo)) {
                log.warn("🚨 URL 회원 번호({})와 인증된 회원 번호({}) 불일치. 접근 거부.", memberNo, authenticatedMemberNo);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("자신이 작성한 댓글만 수정할 수 있습니다.");
            }

            String roleString = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .findFirst()
                    .map(auth -> auth.replace("ROLE_", ""))
                    .orElse("GUEST");
            try {
                currentUserRole = Role.valueOf(roleString);
            } catch (IllegalArgumentException e) {
                log.warn("알 수 없는 역할: {}", roleString);
                currentUserRole = Role.GUEST;
            }
        } else {
            log.warn("🚨 댓글 업데이트 요청 실패: 인증되지 않은 사용자 또는 권한 정보 없음.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        try {
            ReviewCommentResponseDTO responseDTO = reviewCommentService.updateComment(requestDTO, authenticatedMemberNo, currentUserRole);
            log.info("✅ 리뷰 댓글 업데이트 성공: 리뷰 ID {}, 회원 번호 {}", responseDTO.getReviewId(), responseDTO.getMemberNo());
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 리뷰 댓글 업데이트 실패 (권한 부족 또는 잘못된 요청): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 리뷰 댓글 업데이트 실패 (댓글을 찾을 수 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 리뷰 댓글 업데이트 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 DELETE 메서드: 댓글 삭제. URL 경로와 로직 변경!
    @DeleteMapping("/{memberNo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long reviewId,
            @PathVariable Long memberNo, // URL에서 memberNo를 받아옴
            Authentication authentication) {
        log.info("🗑️ 리뷰 댓글 삭제 요청: 리뷰 ID {}, 회원 번호 {}, 요청자: {}", reviewId, memberNo, authentication.getName());

        Long authenticatedMemberNo;
        Role currentUserRole;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            try {
                MemberResponseDTO memberInfo = memberService.getMemberByLoginId(userDetails.getUsername());
                authenticatedMemberNo = memberInfo.getMemberNo();
            } catch (EntityNotFoundException e) {
                log.error("❌ 등록되지 않은 회원으로 댓글 삭제 시도: {}", userDetails.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 사용자 정보입니다.");
            }

            // URL의 memberNo와 인증된 사용자의 memberNo가 다르면 권한 오류
            if (!authenticatedMemberNo.equals(memberNo)) {
                log.warn("🚨 URL 회원 번호({})와 인증된 회원 번호({}) 불일치. 접근 거부.", memberNo, authenticatedMemberNo);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("자신이 작성한 댓글만 삭제할 수 있습니다.");
            }

            String roleString = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .findFirst()
                    .map(auth -> auth.replace("ROLE_", ""))
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

        try {
            // 💡 reviewId와 memberNo를 서비스로 전달
            reviewCommentService.deleteComment(reviewId, memberNo, currentUserRole);
            log.info("✅ 리뷰 댓글 삭제 성공: 리뷰 ID {}, 회원 번호 {}", reviewId, memberNo);
            return ResponseEntity.ok("댓글이 성공적으로 삭제(비활성화)되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 리뷰 댓글 삭제 실패 (권한 부족 또는 잘못된 요청): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 리뷰 댓글 삭제 실패 (댓글을 찾을 수 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 리뷰 댓글 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}