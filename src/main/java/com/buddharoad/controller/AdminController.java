// src/main/java/com/buddharoad/controller/AdminController.java (새로운 AdminController 생성 추천!)
// 또는 기존 MemberController에 @RequestMapping("/api/admin") 으로 분리된 Admin 관련 API들을 모아도 좋아!
package com.buddharoad.controller;
import lombok.extern.slf4j.Slf4j;
import com.buddharoad.dto.Admin.AdminContentSearchFilterDTO;
import com.buddharoad.dto.Admin.AdminStatisticsResponseDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentResponseDTO;
import com.buddharoad.dto.Reviews.Review.ReviewResponseDTO;
import com.buddharoad.dto.Temples.Comment.TempleCommentResponseDTO;
import com.buddharoad.dto.Temples.Temple.TempleResponseDTO;
import com.buddharoad.service.Admin.AdminService; // AdminService 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin") // 관리자 관련 API는 이 경로로 묶는 게 좋아!
@RequiredArgsConstructor
@Log4j2
@Slf4j
public class AdminController {

    private final AdminService adminService; // AdminService 주입

    /**
     * 관리자: 사이트 통계 데이터 조회 (관리자 페이지 - 사이트 통계 탭)
     * GET /api/admin/statistics
     * @return 사이트 통계 데이터 DTO
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'CONTENT_ADMIN')") // ⭐ 관리자 권한만 접근 가능
    public ResponseEntity<AdminStatisticsResponseDTO> getSiteStatistics() {
        log.info("📊 관리자: 사이트 통계 데이터 조회 요청.");
        try {
            AdminStatisticsResponseDTO statistics = adminService.getSiteStatistics();
            log.info("✅ 관리자: 사이트 통계 데이터 조회 성공.");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("❌ 관리자: 사이트 통계 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null); // 500 Internal Server Error
        }
    }
    /**
     * 관리자: 비활성화된 게시글 및 댓글 목록 조회 (콘텐츠 관리 탭)
     * GET /api/admin/contents/inactive
     * @param filter 검색 필터 DTO (boardType, keyword, memberUsername, page, size 등 포함)
     * @return boardType에 따른 페이징 처리된 콘텐츠 목록
     */
    @GetMapping("/contents/inactive")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'CONTENT_ADMIN')")
    public ResponseEntity<?> getInactiveContents(
            @ModelAttribute AdminContentSearchFilterDTO filter) {
        log.info("🔎 관리자: 비활성화된 콘텐츠 목록 조회 요청. 필터: {}", filter);

        try {
            // AdminContentSearchFilterDTO의 boardType에 따라 다른 서비스 호출
            // 반환 타입이 Page<?> 이므로, Map에 담아 유연하게 반환하도록 처리
            Map<String, Page<?>> result = new HashMap<>();

            switch (filter.getBoardType() != null ? filter.getBoardType().toUpperCase() : "") {
                case "TEMPLE":
                    Page<TempleResponseDTO> inactiveTemples = adminService.getInactiveTemples(filter);
                    result.put("temples", inactiveTemples);
                    log.info("✅ 비활성화된 사찰 {}개 조회 성공. 현재 페이지: {}", inactiveTemples.getTotalElements(), inactiveTemples.getNumber());
                    break;
                case "REVIEW":
                    Page<ReviewResponseDTO> inactiveReviews = adminService.getInactiveReviews(filter);
                    result.put("reviews", inactiveReviews);
                    log.info("✅ 비활성화된 리뷰 {}개 조회 성공. 현재 페이지: {}", inactiveReviews.getTotalElements(), inactiveReviews.getNumber());
                    break;
                case "TEMPLE_COMMENT":
                    Page<TempleCommentResponseDTO> inactiveTempleComments = adminService.getInactiveTempleComments(filter);
                    result.put("templeComments", inactiveTempleComments);
                    log.info("✅ 비활성화된 사찰 댓글 {}개 조회 성공. 현재 페이지: {}", inactiveTempleComments.getTotalElements(), inactiveTempleComments.getNumber());
                    break;
                case "REVIEW_COMMENT":
                    Page<ReviewCommentResponseDTO> inactiveReviewComments = adminService.getInactiveReviewComments(filter);
                    result.put("reviewComments", inactiveReviewComments);
                    log.info("✅ 비활성화된 리뷰 댓글 {}개 조회 성공. 현재 페이지: {}", inactiveReviewComments.getTotalElements(), inactiveReviewComments.getNumber());
                    break;
                default:
                    // 모든 비활성화 콘텐츠를 한 번에 가져와야 할 경우 (선택적)
                    // 현재는 프론트에서 게시판 타입을 선택해서 보내는 방식이므로,
                    // 여기에 모든 타입을 다 가져오는 로직을 넣거나, 에러를 반환할 수 있음.
                    log.warn("⚠️ 유효하지 않거나 지정되지 않은 boardType: {}", filter.getBoardType());
                    return ResponseEntity.badRequest().body("유효한 게시판 타입을 지정해야 합니다 (TEMPLE, REVIEW, TEMPLE_COMMENT, REVIEW_COMMENT).");
            }
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ 관리자: 비활성화된 콘텐츠 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("비활성화된 콘텐츠 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}