// src/main/java/com/buddharoad/controller/TempleBookmarkController.java
package com.buddharoad.controller;

import com.buddharoad.domain.Member;
import com.buddharoad.dto.Temples.Bookmark.TempleBookmarkResponseDTO;
import com.buddharoad.service.Temples.TempleBookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize; // 💡💡 이 import 문을 추가해줘! 💡💡
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 💡💡 이 import 문도 추가해줘! 💡💡

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Log4j2
public class TempleBookmarkController {

    private final TempleBookmarkService templeBookmarkService;


    // 💡 찜하기 상태를 토글(추가/삭제)합니다.
    @PostMapping("/temples/{templeId}/bookmarks") // 👈 "/api"는 클래스 레벨에 있으니 생략!
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TempleBookmarkResponseDTO> toggleBookmark(
            @PathVariable Long templeId,
            @AuthenticationPrincipal Member currentMember) {

        log.info("❤️ 사찰 찜하기 토글 요청. 사찰 ID: {}, 요청자: {}", templeId, currentMember.getDisplayName());
        boolean isBookmarked = templeBookmarkService.toggleBookmark(currentMember.getMemberNo(), templeId);

        TempleBookmarkResponseDTO response = TempleBookmarkResponseDTO.builder()
                .templeId(templeId)
                .bookmarked(isBookmarked)
                .build();

        return ResponseEntity.ok(response);
    }

    // 💡 특정 사찰의 찜하기 상태를 조회합니다.
    @GetMapping("/temples/{templeId}/bookmarks/status") // 👈 "/api"는 생략!
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TempleBookmarkResponseDTO> getBookmarkStatus(
            @PathVariable Long templeId,
            @AuthenticationPrincipal Member currentMember) {

        log.info("🔍 사찰 찜하기 상태 조회 요청. 사찰 ID: {}, 요청자: {}", templeId, currentMember.getDisplayName());
        boolean isBookmarked = templeBookmarkService.isBookmarked(currentMember.getMemberNo(), templeId);

        TempleBookmarkResponseDTO response = TempleBookmarkResponseDTO.builder()
                .templeId(templeId)
                .bookmarked(isBookmarked)
                .build();

        return ResponseEntity.ok(response);
    }

    // 💡 로그인한 회원이 찜한 모든 사찰 목록을 조회합니다. (마이페이지용)
    @GetMapping("/members/me/bookmarks") // 👈 "/api"는 생략!
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TempleBookmarkResponseDTO>> getMyBookmarks(
            @AuthenticationPrincipal Member currentMember) {

        log.info("📜 회원 {}번의 찜 목록 조회 요청", currentMember.getMemberNo());
        List<TempleBookmarkResponseDTO> bookmarks = templeBookmarkService.getBookmarksByMemberNo(currentMember.getMemberNo());

        if (bookmarks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(bookmarks);
    }
}