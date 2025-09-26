// src/main/java/com/buddharoad/service/Temples/TempleCommentService.java
package com.buddharoad.service.Temples;

import com.buddharoad.dto.Temples.Comment.TempleCommentRegisterRequestDTO;
import com.buddharoad.dto.Temples.Comment.TempleCommentResponseDTO;
import com.buddharoad.dto.Temples.Comment.TempleCommentSearchFilterDTO;
import com.buddharoad.dto.Temples.Comment.TempleCommentUpdateRequestDTO;
import com.buddharoad.security.Role; // 권한 처리를 위해 Role Enum import
import org.springframework.data.domain.Page;
import com.buddharoad.dto.Admin.AdminContentSearchFilterDTO;

public interface TempleCommentService {

    /**
     * 특정 사찰에 새 댓글을 등록합니다.
     *
     * @param requestDTO 댓글 등록 요청 DTO
     * @param memberNo   댓글을 작성하는 회원의 고유 번호
     * @param role       댓글을 작성하는 사용자의 역할 (권한 확인용)
     * @return 등록된 댓글의 응답 DTO
     */
    TempleCommentResponseDTO registerComment(TempleCommentRegisterRequestDTO requestDTO, Long memberNo, Role role);

    /**
     * 특정 댓글의 상세 정보를 조회합니다.
     * 비회원도 조회 가능하지만, 조회수 증가 로직은 회원/비회원 구분하여 처리합니다.
     *
     * @param templeCommentId 조회할 댓글의 ID
     * @param userId          요청하는 사용자의 ID (조회수 중복 방지용)
     * @return 댓글 상세 응답 DTO
     */
    TempleCommentResponseDTO getCommentDetails(Long templeCommentId, String userId);

    /**
     * 특정 사찰의 댓글 목록을 검색 조건에 따라 페이징하여 조회합니다.
     * 목록 조회 시에도 조회수 증가 로직을 적용합니다.
     *
     * @param templeId  댓글이 속한 사찰의 ID
     * @param filterDTO 검색 및 페이징 필터 DTO
     * @param userId    요청하는 사용자의 ID (조회수 중복 방지용)
     * @return 페이징 처리된 댓글 응답 DTO 목록
     */
    Page<TempleCommentResponseDTO> searchComments(Long templeId, TempleCommentSearchFilterDTO filterDTO, String userId);

    /**
     * 특정 댓글의 내용을 수정합니다.
     * 작성자 본인 또는 관리자만 수정 가능합니다.
     *
     * @param requestDTO 댓글 수정 요청 DTO (댓글 ID, 사찰 ID, 내용 포함)
     * @param memberNo   수정을 요청하는 회원의 고유 번호
     * @param role       수정을 요청하는 사용자의 역할 (권한 확인용)
     * @return 수정된 댓글의 응답 DTO
     */
    TempleCommentResponseDTO updateComment(TempleCommentUpdateRequestDTO requestDTO, Long memberNo, Role role);

    /**
     * 특정 댓글을 소프트 삭제 처리합니다.
     * 작성자 본인 또는 관리자만 삭제 가능합니다.
     *
     * @param templeCommentId 삭제할 댓글의 ID
     * @param memberNo        삭제를 요청하는 회원의 고유 번호
     * @param role            삭제를 요청하는 사용자의 역할 (권한 확인용)
     */
    // ⭐⭐ 핵심 수정: deleteComment 메서드의 파라미터를 추가하여 구현체와 시그니처를 일치시킵니다. ⭐⭐
    void deleteComment(Long templeCommentId, Long memberNo, Role role);


    // ⭐⭐ 이 메서드는 이미 추가되어 있었으니 그대로 유지합니다. ⭐⭐
    Page<TempleCommentResponseDTO> getMyTempleComments(Long memberNo, String keyword, int page, int size, String sortBy, String sortOrder);

    // --- ⭐ 관리자용 API 메서드 추가 ⭐ ---
    /**
     * 관리자용: 삭제된 사찰 댓글 목록을 검색하고 페이징 처리하여 반환한다.
     * (사찰 댓글은 isActive 필드가 없는 것으로 보이며, isDeleted 필드를 기준으로 판단)
     * @param filter 검색 필터 (keyword, memberUsername, page, size, sortBy, sortOrder 등 포함)
     * @return 페이징 처리된 TempleCommentResponseDTO 목록
     */
    Page<TempleCommentResponseDTO> searchInactiveTempleCommentsForAdmin(AdminContentSearchFilterDTO filter);
    // --- ⭐ 관리자용 API 메서드 추가 끝 ⭐ ---

}