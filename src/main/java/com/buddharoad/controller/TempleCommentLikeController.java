package com.buddharoad.controller;

import com.buddharoad.domain.Member;
import com.buddharoad.dto.Temples.Likes.TempleCommentLikeResponseDTO;
import com.buddharoad.service.Temples.TempleCommentLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/temples/comments") // 💡 댓글 관련 엔드포인트는 여기에 모으자
@Log4j2
public class TempleCommentLikeController {

    private final TempleCommentLikeService templeCommentLikeService;

    // ✅ 댓글 '좋아요' 상태를 토글(추가/삭제)합니다.
    @PostMapping("/{templeCommentId}/likes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TempleCommentLikeResponseDTO> toggleLike(
            @PathVariable Long templeCommentId,
            @AuthenticationPrincipal Member currentMember) {

        log.info("❤️ 댓글 좋아요 토글 요청. 댓글 ID: {}, 요청자: {}", templeCommentId, currentMember.getDisplayName());
        boolean isLiked = templeCommentLikeService.toggleLike(currentMember.getMemberNo(), templeCommentId);

        TempleCommentLikeResponseDTO response = TempleCommentLikeResponseDTO.builder()
                .templeCommentId(templeCommentId)
                .isLiked(isLiked)
                .build();

        return ResponseEntity.ok(response);
    }

    // ✅ 특정 댓글의 '좋아요' 상태를 조회합니다.
    @GetMapping("/{templeCommentId}/likes/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TempleCommentLikeResponseDTO> getLikeStatus(
            @PathVariable Long templeCommentId,
            @AuthenticationPrincipal Member currentMember) {

        log.info("🔍 댓글 좋아요 상태 조회 요청. 댓글 ID: {}, 요청자: {}", templeCommentId, currentMember.getDisplayName());
        boolean isLiked = templeCommentLikeService.isLiked(currentMember.getMemberNo(), templeCommentId);

        TempleCommentLikeResponseDTO response = TempleCommentLikeResponseDTO.builder()
                .templeCommentId(templeCommentId)
                .isLiked(isLiked)
                .build();

        return ResponseEntity.ok(response);
    }

    // ✅ 로그인한 회원이 '좋아요'한 모든 댓글 목록을 조회합니다. (마이페이지용)
    @GetMapping("/me/likes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TempleCommentLikeResponseDTO>> getMyLikes(
            @AuthenticationPrincipal Member currentMember) {

        log.info("📜 회원 {}번의 좋아요 목록 조회 요청", currentMember.getMemberNo());
        List<TempleCommentLikeResponseDTO> likes = templeCommentLikeService.getLikesByMemberNo(currentMember.getMemberNo());

        if (likes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(likes);
    }
}