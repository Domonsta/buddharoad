package com.buddharoad.controller;

import com.buddharoad.domain.Member;
import com.buddharoad.dto.Reviews.Review.ReviewRegisterRequestDTO;
import com.buddharoad.dto.Reviews.Review.ReviewResponseDTO;
import com.buddharoad.dto.Reviews.Review.ReviewSearchFilterDTO;
import com.buddharoad.dto.Reviews.Review.ReviewUpdateRequestDTO;
import com.buddharoad.security.Role;
import com.buddharoad.service.Reviews.ReviewService;
import com.buddharoad.service.Member.MemberService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // ✨ 이 import 문은 혹시 빠졌을까 봐 추가했어!
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews") // 이 부분이 중요해! 모든 리뷰 관련 요청은 /api/reviews로 시작해야 해.
@RequiredArgsConstructor
@Log4j2
public class ReviewController {

    private final ReviewService reviewService;
    private final MemberService memberService; // MemberService 주입

    // 💡 리뷰 등록 (POST) - 사진 파일 포함
    @PostMapping(value = "/register", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> registerReview(
            @Valid @RequestPart("request") ReviewRegisterRequestDTO requestDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal Member currentMember, // 인증된 사용자 정보 가져오기
            BindingResult bindingResult) {

        log.info("📝 리뷰 등록 요청: 사용자='{}'", currentMember.getDisplayName());

        if (bindingResult.hasErrors()) {
            log.warn("⚠️ 유효성 검사 오류: {}", bindingResult.getAllErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }
        if (currentMember == null) {
            log.warn("🚨 리뷰 등록 요청 실패: 인증되지 않은 사용자.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        try {
            ReviewResponseDTO responseDTO = reviewService.registerReview(requestDTO, files, currentMember);
            log.info("✅ 리뷰 등록 성공: ID {}", responseDTO.getReviewId());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 리뷰 등록 실패 ({}): {}", e.getClass().getSimpleName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 리뷰 등록 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 리뷰 등록 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 리뷰 목록 조회 (GET)
    @GetMapping
    public ResponseEntity<Page<ReviewResponseDTO>> searchReviews(ReviewSearchFilterDTO filterDTO) {
        log.info("🔍 리뷰 목록 조회 요청: 필터={}", filterDTO);
        Page<ReviewResponseDTO> reviews = reviewService.searchReviews(filterDTO);
        log.info("✅ 리뷰 목록 조회 성공: {}개 리뷰 발견", reviews.getTotalElements());
        return ResponseEntity.ok(reviews);
    }

    // 💡 특정 리뷰 상세 조회 (GET) - 조회수 증가 로직 포함
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewDetails(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal(expression = "username") String userLoginId // 로그인 ID만 필요
    ) {
        // ✨ [로그 Controller] 컨트롤러 진입 확인!
        // 이 로그가 안 찍히면 프론트엔드 요청 URL이나 백엔드 URL 매핑 문제일 가능성이 커!
        log.info(">>>> [Controller] ReviewController.getReviewDetails 진입. 요청 Review ID={}, 사용자 로그인 ID='{}'", reviewId, userLoginId);
        try {
            ReviewResponseDTO review = reviewService.getReviewDetails(reviewId, userLoginId);
            log.info("✅ 리뷰 상세 조회 성공: ID {}", reviewId);
            return ResponseEntity.ok(review);
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 리뷰 상세 조회 실패 (리뷰를 찾을 수 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 리뷰 상세 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 상세 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 리뷰 수정 (PUT) - 사진 파일 포함
    @PutMapping(value = "/{reviewId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestPart("request") ReviewUpdateRequestDTO requestDTO,
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles, // 새로 추가할 파일
            @RequestParam(value = "deletedPhotoIds", required = false) List<Long> deletedPhotoIds, // 삭제할 사진 ID 목록
            @AuthenticationPrincipal Member currentMember, // 인증된 사용자 정보 가져오기
            BindingResult bindingResult) {

        log.info("🔄 리뷰 수정 요청: ID={}, 사용자='{}'", reviewId, currentMember.getDisplayName());

        if (bindingResult.hasErrors()) {
            log.warn("⚠️ 유효성 검사 오류: {}", bindingResult.getAllErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }
        if (currentMember == null) {
            log.warn("🚨 리뷰 수정 요청 실패: 인증되지 않은 사용자.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        try {
            ReviewResponseDTO responseDTO = reviewService.updateReview(reviewId, requestDTO, newFiles, deletedPhotoIds, currentMember);
            log.info("✅ 리뷰 수정 성공: ID {}", responseDTO.getReviewId());
            return ResponseEntity.ok(responseDTO);
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 리뷰 수정 실패 (리뷰를 찾을 수 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 리뷰 수정 실패 (권한 부족 또는 잘못된 요청): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 리뷰 수정 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 리뷰 소프트 삭제 (PATCH)
    @PatchMapping("/{reviewId}/soft-delete")
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<String> softDeleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Member currentMember // 인증된 사용자 정보 가져오기
    ) {
        log.info("🗑️ 리뷰 소프트 삭제 요청: ID={}, 사용자='{}', 권한='{}'", reviewId, currentMember.getDisplayName(), currentMember.getRole());
        try {
            if (currentMember == null) {
                log.warn("🚨 리뷰 소프트 삭제 요청 실패: 인증되지 않은 사용자.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
            }
            reviewService.softDeleteReview(reviewId, currentMember);
            log.info("✅ 리뷰 소프트 삭제 성공: ID {}", reviewId);
            return ResponseEntity.ok("리뷰가 성공적으로 삭제(비활성화)되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 리뷰 삭제 실패 (권한 부족 또는 잘못된 요청): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 리뷰 삭제 실패 (리뷰를 찾을 수 없음): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 리뷰 소프트 삭제 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 소프트 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 리뷰 활성화/비활성화 상태 변경 (PATCH)
    @PatchMapping("/{reviewId}/status")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> changeReviewActiveStatus(
            @PathVariable Long reviewId,
            @RequestParam Boolean isActive,
            @AuthenticationPrincipal Member currentMember
    ) {
        log.info("⚡️ 리뷰 활성화 상태 변경 요청: ID={}, isActive: {}, 사용자='{}' 권한='{}'", reviewId, isActive, currentMember.getDisplayName(), currentMember.getRole());
        try {
            if (currentMember == null) {
                log.warn("🚨 리뷰 활성화 상태 변경 요청 실패: 인증되지 않은 사용자.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
            }

            reviewService.changeReviewActiveStatus(reviewId, isActive, currentMember.getRole());
            log.info("✅ 리뷰 활성화 상태 변경 성공: ID={}, isActive: {}", reviewId, isActive);
            return ResponseEntity.ok("리뷰 활성화 상태가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 권한 없음 또는 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 리뷰를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 리뷰 활성화 상태 변경 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 활성화 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/api/auth/me/reviews")
    @PreAuthorize("hasAnyRole('USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<Page<ReviewResponseDTO>> getMyReviews(
            @AuthenticationPrincipal Member currentMember, // 현재 로그인한 회원 정보를 자동으로 가져와!
            @ModelAttribute ReviewSearchFilterDTO filterDTO) {

        log.info("🔎 내 리뷰 목록 조회 요청: 회원='{}', 필터={}", currentMember.getDisplayName(), filterDTO);

        // ReviewService의 getMyReviews 메서드 호출
        Page<ReviewResponseDTO> myReviews = reviewService.getMyReviews(currentMember.getMemberNo(), filterDTO);

        return ResponseEntity.ok(myReviews);
    }
}