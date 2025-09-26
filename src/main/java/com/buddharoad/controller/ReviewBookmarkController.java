package com.buddharoad.controller; // 💡 리뷰 관련 패키지 경로로 변경

import com.buddharoad.domain.Member;
import com.buddharoad.dto.Reviews.Bookmark.ReviewBookmarkResponseDTO; // 💡 리뷰용 DTO 임포트
import com.buddharoad.service.Reviews.ReviewBookmarkService; // 💡 리뷰용 서비스 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews") // 💡 리뷰 관련 엔드포인트는 여기에 모으자
@Log4j2
public class ReviewBookmarkController {

    private final ReviewBookmarkService reviewBookmarkService;

    // ✅ 리뷰 찜하기 상태를 토글(추가/삭제)합니다.
    @PostMapping("/{reviewId}/bookmarks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewBookmarkResponseDTO> toggleBookmark(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Member currentMember) {

        log.info("❤️ 리뷰 찜하기 토글 요청. 리뷰 ID: {}, 요청자: {}", reviewId, currentMember.getDisplayName());
        boolean isBookmarked = reviewBookmarkService.toggleBookmark(currentMember.getMemberNo(), reviewId);

        ReviewBookmarkResponseDTO response = ReviewBookmarkResponseDTO.builder()
                .reviewId(reviewId)
                .isBookmarked(isBookmarked)
                .build();

        return ResponseEntity.ok(response);
    }

    // ✅ 특정 리뷰의 찜하기 상태를 조회합니다.
    @GetMapping("/{reviewId}/bookmarks/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewBookmarkResponseDTO> getBookmarkStatus(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Member currentMember) {

        log.info("🔍 리뷰 찜하기 상태 조회 요청. 리뷰 ID: {}, 요청자: {}", reviewId, currentMember.getDisplayName());
        boolean isBookmarked = reviewBookmarkService.isBookmarked(currentMember.getMemberNo(), reviewId);

        ReviewBookmarkResponseDTO response = ReviewBookmarkResponseDTO.builder()
                .reviewId(reviewId)
                .isBookmarked(isBookmarked)
                .build();

        return ResponseEntity.ok(response);
    }

    // ✅ 로그인한 회원이 찜한 모든 리뷰 목록을 조회합니다. (마이페이지용)
    @GetMapping("/me/bookmarks") // 💡 /api/reviews/me/bookmarks
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReviewBookmarkResponseDTO>> getMyBookmarks(
            @AuthenticationPrincipal Member currentMember) {

        log.info("📜 회원 {}번의 찜 목록 조회 요청", currentMember.getMemberNo());
        List<ReviewBookmarkResponseDTO> bookmarks = reviewBookmarkService.getBookmarksByMemberNo(currentMember.getMemberNo());

        if (bookmarks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(bookmarks);
    }
}