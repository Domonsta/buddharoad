package com.buddharoad.controller;

import com.buddharoad.domain.Member;
import com.buddharoad.dto.Reviews.Likes.ReviewLikeResponseDTO;
import com.buddharoad.service.Reviews.ReviewLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewLikeController {

    private final ReviewLikeService reviewLikeService;

    @PostMapping("/{reviewId}/likes")
    public ResponseEntity<ReviewLikeResponseDTO> toggleLike(@PathVariable Long reviewId, @AuthenticationPrincipal Member member) {

        // JWT 토큰에서 로그인된 회원의 memberNo를 가져와서 서비스로 전달
        Long memberNo = member.getMemberNo();
        ReviewLikeResponseDTO response = reviewLikeService.toggleLike(reviewId, memberNo);

        return ResponseEntity.ok(response);
    }

    // ⭐⭐ 리뷰 좋아요 상태를 확인하는 GET API 추가 ⭐⭐
    // ⭐⭐ getLikeStatus 메서드 수정 ⭐⭐
    @GetMapping("/{reviewId}/likes/status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(@PathVariable Long reviewId, @AuthenticationPrincipal Optional<Member> member) {
        Map<String, Object> response = new HashMap<>();

        // 좋아요 개수를 가져오는 서비스 메서드 호출
        Long likeCount = reviewLikeService.getLikeCount(reviewId);
        response.put("likeCount", likeCount);

        // ⭐⭐⭐ member가 null이 아닌지 먼저 체크! ⭐⭐⭐
        boolean isLiked = false;
        if (member != null && member.isPresent()) {
            isLiked = reviewLikeService.checkIfLiked(reviewId, member.get().getMemberNo());
        }
        response.put("isLiked", isLiked);

        return ResponseEntity.ok(response);
    }
}