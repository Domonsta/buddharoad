// src/main/java/com/buddharoad/controller/ReviewCommentLikeController.java
package com.buddharoad.controller;

import com.buddharoad.dto.Reviews.Likes.ReviewCommentLikeResponseDTO;
import com.buddharoad.service.Reviews.ReviewCommentLikeService;
import com.buddharoad.domain.Member; // 💡 Member 엔티티를 직접 임포트
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews/{reviewId}/comments/{reviewCommentMemberNo}/likes")
@RequiredArgsConstructor
@Log4j2
public class ReviewCommentLikeController {

    private final ReviewCommentLikeService reviewCommentLikeService;

    /**
     * 댓글 좋아요 토글 (추가/취소)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long reviewId,
            @PathVariable Long reviewCommentMemberNo,
            @AuthenticationPrincipal Member currentUser) { // 💡 CustomUserDetails 대신 Member 사용

        log.info("📧 댓글 좋아요 토글 요청. 리뷰 ID: {}, 댓글 작성자 회원 번호: {}, 요청자: {}",
                reviewId, reviewCommentMemberNo, currentUser.getLoginId()); // 💡 getLoginId() 사용

        try {
            ReviewCommentLikeResponseDTO responseDTO = reviewCommentLikeService.toggleLike(
                    reviewId,
                    reviewCommentMemberNo,
                    currentUser.getMemberNo()); // 💡 getMemberNo() 사용
            return ResponseEntity.ok(responseDTO);
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 좋아요 토글 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 좋아요 토글 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("좋아요 토글 중 오류가 발생했습니다.");
        }
    }

    /**
     * 댓글 좋아요 개수 조회
     */
    @GetMapping
    public ResponseEntity<?> getLikeCount(
            @PathVariable Long reviewId,
            @PathVariable Long reviewCommentMemberNo) {

        log.info("🔍 댓글 좋아요 수 조회 요청. 리뷰 ID: {}, 댓글 작성자 회원 번호: {}",
                reviewId, reviewCommentMemberNo);

        try {
            Long likeCount = reviewCommentLikeService.getLikeCount(reviewId, reviewCommentMemberNo);
            return ResponseEntity.ok(likeCount);
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 좋아요 수 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 좋아요 수 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("좋아요 수 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 특정 회원의 댓글 좋아요 여부 확인
     */
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkIfLiked(
            @PathVariable Long reviewId,
            @PathVariable Long reviewCommentMemberNo,
            @AuthenticationPrincipal Member currentUser) { // 💡 CustomUserDetails 대신 Member 사용

        log.info("✅ 댓글 좋아요 여부 확인 요청. 리뷰 ID: {}, 댓글 작성자 회원 번호: {}, 요청자: {}",
                reviewId, reviewCommentMemberNo, currentUser.getLoginId()); // 💡 getLoginId() 사용

        try {
            boolean isLiked = reviewCommentLikeService.checkIfLiked(reviewId, reviewCommentMemberNo, currentUser.getMemberNo()); // 💡 getMemberNo() 사용
            return ResponseEntity.ok(isLiked);
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 좋아요 여부 확인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 좋아요 여부 확인 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("좋아요 여부 확인 중 오류가 발생했습니다.");
        }
    }
}