// src/main/java/com/buddharoad/service/Reviews/ReviewCommentService.java
package com.buddharoad.service.Reviews;

import com.buddharoad.dto.Admin.AdminContentSearchFilterDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentRegisterRequestDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentResponseDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentSearchFilterDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentUpdateRequestDTO;
import com.buddharoad.security.Role;
import org.springframework.data.domain.Page;

public interface ReviewCommentService {

    /**
     * 새로운 리뷰 댓글을 등록합니다.
     * @param requestDTO 댓글 등록 요청 DTO
     * @param memberNo 댓글을 작성하는 회원의 고유 번호
     * @param memberUsername 댓글을 작성하는 회원의 닉네임 (username)
     * @return 등록된 댓글의 응답 DTO
     */
    ReviewCommentResponseDTO registerComment(ReviewCommentRegisterRequestDTO requestDTO, Long memberNo, String memberUsername);

    /**
     * 특정 리뷰 댓글을 조회합니다. (조회수 로직 제거)
     * @param reviewId 조회할 댓글이 속한 리뷰의 ID
     * @param memberNo 조회할 댓글의 작성자 회원 번호
     * @return 조회된 댓글의 응답 DTO
     */
    ReviewCommentResponseDTO getComment(Long reviewId, Long memberNo);

    /**
     * 리뷰 댓글을 업데이트합니다.
     * @param requestDTO 댓글 업데이트 요청 DTO
     * @param memberNo 댓글을 수정하는 회원의 고유 번호
     * @param role 댓글을 수정하는 회원의 역할 (권한 검사용)
     * @return 업데이트된 댓글의 응답 DTO
     */
    ReviewCommentResponseDTO updateComment(ReviewCommentUpdateRequestDTO requestDTO, Long memberNo, Role role);

    /**
     * 특정 리뷰 댓글을 소프트 삭제(비활성화)합니다.
     * @param reviewId 삭제할 댓글이 속한 리뷰의 ID
     * @param memberNo 삭제를 요청하는 회원의 고유 번호
     * @param role 삭제를 요청하는 회원의 역할 (권한 검사용)
     */
    void deleteComment(Long reviewId, Long memberNo, Role role);

    /**
     * 검색 필터에 따라 리뷰 댓글 목록을 페이징하여 조회합니다.
     * @param filter 검색 조건 DTO
     * @return 페이징 처리된 댓글 목록 DTO
     */
    Page<ReviewCommentResponseDTO> searchComments(ReviewCommentSearchFilterDTO filter);

    /**
     * 특정 리뷰의 활성 댓글 총 개수를 조회합니다.
     * @param reviewId 댓글이 속한 리뷰의 ID
     * @return 해당 리뷰의 활성 댓글 총 개수
     */
    long countActiveCommentsByReviewId(Long reviewId);

    /**
     * 특정 회원이 작성한 리뷰 댓글 목록 조회 (마이페이지용)
     * @param memberNo 회원 고유 번호
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortOrder 정렬 순서
     * @return 페이징 처리된 댓글 목록
     */
    Page<ReviewCommentResponseDTO> getMyReviewComments(Long memberNo, String keyword, int page, int size, String sortBy, String sortOrder);

    /**
     * 관리자용: 삭제된 리뷰 댓글 목록을 검색하고 페이징 처리하여 반환한다.
     * @param filter 검색 필터
     * @return 페이징 처리된 ReviewCommentResponseDTO 목록
     */
    Page<ReviewCommentResponseDTO> searchInactiveReviewCommentsForAdmin(AdminContentSearchFilterDTO filter);
}