package com.buddharoad.service.Reviews;

import com.buddharoad.domain.Member; // Member 엔티티 import 추가!
import com.buddharoad.dto.Reviews.Review.ReviewRegisterRequestDTO;
import com.buddharoad.dto.Reviews.Review.ReviewResponseDTO;
import com.buddharoad.dto.Reviews.Review.ReviewSearchFilterDTO;
import com.buddharoad.dto.Reviews.Review.ReviewUpdateRequestDTO;
import com.buddharoad.security.Role;
import com.buddharoad.dto.Admin.AdminContentSearchFilterDTO;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {
    // ⭐ registerReview: Role과 Member 객체를 함께 받도록 변경
    ReviewResponseDTO registerReview(ReviewRegisterRequestDTO requestDTO, List<MultipartFile> files, Member currentMember);

    // ✨ 이 메서드가 ReviewResponseDTO에 region을 포함하여 반환할 거야. ✨
    Page<ReviewResponseDTO> searchReviews(ReviewSearchFilterDTO filterDTO);

    // ✨ 이 메서드가 ReviewResponseDTO에 region을 포함하여 반환할 거야. ✨
    ReviewResponseDTO getReviewDetails(Long reviewId, String userLoginId);

    // ⭐ updateReview: Role과 Member 객체를 함께 받도록 변경
    ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateRequestDTO requestDTO, List<MultipartFile> newFiles, List<Long> deletedPhotoIds, Member currentMember);

    // ⭐ softDeleteReview: Member 객체를 받도록 변경
    void softDeleteReview(Long reviewId, Member currentMember);

    // 💡💡💡 이 부분을 추가해줘! 💡💡💡
    void changeReviewActiveStatus(Long reviewId, Boolean isActive, Role role);

    // ⭐ 추가: 특정 회원이 작성한 리뷰 목록 조회 (마이페이지용)
    Page<ReviewResponseDTO> getMyReviews(Long memberNo, ReviewSearchFilterDTO filterDTO);

    // --- ⭐ 관리자용 API 메서드 추가 ⭐ ---
    /**
     * 관리자용: 비활성화되거나 삭제된 리뷰 목록을 검색하고 페이징 처리하여 반환한다.
     * @param filter 검색 필터 (keyword, memberUsername, page, size, sortBy, sortOrder 등 포함)
     * @return 페이징 처리된 ReviewResponseDTO 목록
     */
    Page<ReviewResponseDTO> searchInactiveReviewsForAdmin(AdminContentSearchFilterDTO filter);
    // --- ⭐ 관리자용 API 메서드 추가 끝 ⭐ ---
}