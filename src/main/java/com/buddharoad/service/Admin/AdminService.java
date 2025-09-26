// src/main/java/com/buddharoad/service/Admin/AdminService.java
package com.buddharoad.service.Admin;

import com.buddharoad.dto.Admin.AdminContentSearchFilterDTO;
import com.buddharoad.dto.Admin.AdminStatisticsResponseDTO;
import com.buddharoad.dto.Reviews.Review.ReviewResponseDTO; // ReviewResponseDTO 임포트
import com.buddharoad.dto.Reviews.Comment.ReviewCommentResponseDTO; // ReviewCommentResponseDTO 임포트
import com.buddharoad.dto.Temples.Temple.TempleResponseDTO; // TempleResponseDTO 임포트
import com.buddharoad.dto.Temples.Comment.TempleCommentResponseDTO; // TempleCommentResponseDTO 임포트
import org.springframework.data.domain.Page; // Page 임포트 (추가 확인)

// ⭐⭐⭐ 여기가 핵심! class를 interface로 변경해야 해! ⭐⭐⭐
public interface AdminService { // 'class'를 'interface'로 변경

    // 인터페이스에서는 @Transactional 어노테이션을 메서드 선언부에 직접 붙이지 않아.
    // 구현체(ServiceImpl)에서 붙여줄 거야.
    AdminStatisticsResponseDTO getSiteStatistics();

    /**
     * 비활성화된 사찰 목록을 검색하고 페이징 처리하여 반환한다.
     * @param filter 검색 필터 (keyword, memberUsername, page, size, sortBy, sortOrder 등)
     * @return 페이징 처리된 TempleResponseDTO 목록
     */
    Page<TempleResponseDTO> getInactiveTemples(AdminContentSearchFilterDTO filter);

    /**
     * 비활성화된 리뷰 목록을 검색하고 페이징 처리하여 반환한다.
     * @param filter 검색 필터 (keyword, memberUsername, page, size, sortBy, sortOrder 등)
     * @return 페이징 처리된 ReviewResponseDTO 목록
     */
    Page<ReviewResponseDTO> getInactiveReviews(AdminContentSearchFilterDTO filter);

    /**
     * 비활성화된 사찰 댓글 목록을 검색하고 페이징 처리하여 반환한다.
     * @param filter 검색 필터 (keyword, memberUsername, page, size, sortBy, sortOrder 등)
     * @return 페이징 처리된 TempleCommentResponseDTO 목록
     */
    Page<TempleCommentResponseDTO> getInactiveTempleComments(AdminContentSearchFilterDTO filter);

    /**
     * 비활성화된 리뷰 댓글 목록을 검색하고 페이징 처리하여 반환한다.
     * @param filter 검색 필터 (keyword, memberUsername, page, size, sortBy, sortOrder 등)
     * @return 페이징 처리된 ReviewCommentResponseDTO 목록
     */
    Page<ReviewCommentResponseDTO> getInactiveReviewComments(AdminContentSearchFilterDTO filter);
}