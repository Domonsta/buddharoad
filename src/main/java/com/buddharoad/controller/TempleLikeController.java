package com.buddharoad.controller;

import com.buddharoad.domain.Member;
import com.buddharoad.dto.Temples.Likes.TempleLikeResponseDTO;
import com.buddharoad.service.Temples.TempleLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/temples")
@Log4j2
public class TempleLikeController {

    private final TempleLikeService templeLikeService;

    // ✅ 사찰 '좋아요' 상태를 토글(추가/삭제)합니다.
    @PostMapping("/{templeId}/likes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TempleLikeResponseDTO> toggleLike(
            @PathVariable Long templeId,
            @AuthenticationPrincipal Member currentMember) {

        log.info("❤️ 사찰 좋아요 토글 요청. 사찰 ID: {}, 요청자: {}", templeId, currentMember.getDisplayName());
        boolean isLiked = templeLikeService.toggleLike(currentMember.getMemberNo(), templeId);

        TempleLikeResponseDTO response = TempleLikeResponseDTO.builder()
                .templeId(templeId)
                .isLiked(isLiked)
                .build();

        return ResponseEntity.ok(response);
    }

    // ✅ 특정 사찰의 '좋아요' 상태를 조회합니다.
    @GetMapping("/{templeId}/likes/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TempleLikeResponseDTO> getLikeStatus(
            @PathVariable Long templeId,
            @AuthenticationPrincipal Member currentMember) {

        log.info("🔍 사찰 좋아요 상태 조회 요청. 사찰 ID: {}, 요청자: {}", templeId, currentMember.getDisplayName());
        boolean isLiked = templeLikeService.isLiked(currentMember.getMemberNo(), templeId);

        TempleLikeResponseDTO response = TempleLikeResponseDTO.builder()
                .templeId(templeId)
                .isLiked(isLiked)
                .build();

        return ResponseEntity.ok(response);
    }

    // ✅ 로그인한 회원이 '좋아요'한 모든 사찰 목록을 조회합니다. (마이페이지용)
    @GetMapping("/me/likes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TempleLikeResponseDTO>> getMyLikes(
            @AuthenticationPrincipal Member currentMember) {

        log.info("📜 회원 {}번의 좋아요 목록 조회 요청", currentMember.getMemberNo());
        List<TempleLikeResponseDTO> likes = templeLikeService.getLikesByMemberNo(currentMember.getMemberNo());

        if (likes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(likes);
    }
}