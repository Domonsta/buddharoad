package com.buddharoad.repository.Temple;

import com.buddharoad.domain.TempleComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 💡 JpaSpecificationExecutor 임포트
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TempleCommentRepository extends JpaRepository<TempleComment, Long>, JpaSpecificationExecutor<TempleComment> { // 💡 JpaSpecificationExecutor 추가
    // JpaRepository를 상속받으면 TempleComment 엔티티와 Long 타입의 ID를 기반으로
    // 기본적인 CRUD 메서드 (save, findById, findAll, delete 등)가 자동으로 제공돼.
    // JpaSpecificationExecutor를 추가하여 Specification 기반의 동적 쿼리 기능을 사용할 수 있게 돼.

    // --- 추가적으로 필요한 쿼리 메서드 예시 ---

    // 1. 특정 댓글 ID로 활성 댓글을 조회하는 메서드 (단일 조회)
    Optional<TempleComment> findByTempleCommentIdAndIsDeletedFalse(Long templeCommentId);

    // 2. 특정 사찰(Temple)에 달린 활성 댓글을 페이징하여 조회하는 메서드 (목록 조회)
    // TempleCommentSearchFilterDTO의 sortBy, sortOrder, page, size를 Pageable 객체가 처리합니다.
    // content 검색, minViewCount, minLikeCount는 Service 계층에서 Specification 또는 @Query로 조합될 수 있습니다.
    Page<TempleComment> findByTempleTempleIdAndIsDeletedFalse(Long templeId, Pageable pageable);


    // 3. 특정 사찰의 댓글 중 내용(content)을 포함하는 활성 댓글을 페이징하여 조회
    // 대소문자 구분 없이 검색 (ContainingIgnoreCase)
    Page<TempleComment> findByTempleTempleIdAndContentContainingIgnoreCaseAndIsDeletedFalse(Long templeId, String content, Pageable pageable);

    // 4. 특정 사찰의 활성 댓글 총 개수를 조회하는 메서드
    // 댓글 상세보기에 댓글 수를 뿌려주기 위함
    long countByTempleTempleIdAndIsDeletedFalse(Long templeId);

    // 5. 특정 회원이 작성한 모든 사찰 댓글을 조회하는 메서드
    // TempleComment 엔티티의 'member' 필드를 기준으로 검색
    List<TempleComment> findByMemberMemberNo(Long memberNo);

    // 6. 조회수(viewCount) 기준으로 정렬된 특정 사찰의 활성 댓글을 페이징하여 조회
    // Pageable에 Sort.by("viewCount", Sort.Direction.DESC) 등을 포함하여 호출하면 됩니다.
    Page<TempleComment> findByTempleTempleIdAndIsDeletedFalseOrderByViewCountDesc(Long templeId, Pageable pageable);

    // 7. 좋아요 수(likeCount) 기준으로 정렬된 특정 사찰의 활성 댓글을 페이징하여 조회
    // 좋아요 수는 TempleCommentLike 컬렉션의 크기이므로, JPQL에서는 직접 정렬하기 까다롭습니다.
    // @Query 어노테이션을 사용하여 JOIN FETCH와 GROUP BY를 활용해야 합니다.
    // 이 쿼리는 N+1 문제를 방지하고 좋아요 수를 기준으로 정렬합니다.
    @Query("SELECT tc FROM TempleComment tc LEFT JOIN tc.templeCommentLikes tcl WHERE tc.temple.templeId = :templeId AND tc.isDeleted = FALSE GROUP BY tc ORDER BY COUNT(tcl) DESC")
    Page<TempleComment> findByTempleIdOrderByLikeCountDesc(@Param("templeId") Long templeId, Pageable pageable);

    // 좋아요 수로 오름차순 정렬
    @Query("SELECT tc FROM TempleComment tc LEFT JOIN tc.templeCommentLikes tcl WHERE tc.temple.templeId = :templeId AND tc.isDeleted = FALSE GROUP BY tc ORDER BY COUNT(tcl) ASC")
    Page<TempleComment> findByTempleIdOrderByLikeCountAsc(@Param("templeId") Long templeId, Pageable pageable);

    // ⭐⭐ 추가: 특정 회원이 작성한 활성 사찰 댓글을 페이징하여 조회
    Page<TempleComment> findByMemberMemberNoAndIsDeletedFalseAndIsActiveTrue(Long memberNo, Pageable pageable);

    // ⭐⭐ 추가: 특정 회원이 작성한 활성 사찰 댓글 중 내용이나 원본 사찰 이름으로 검색 (JPQL 사용)
    @Query("SELECT tc FROM TempleComment tc JOIN tc.temple t " +
            "WHERE tc.member.memberNo = :memberNo AND tc.isDeleted = FALSE AND tc.isActive = TRUE " +
            "AND (:keyword IS NULL OR LOWER(tc.content) LIKE CONCAT('%', LOWER(:keyword), '%') OR LOWER(t.templeName) LIKE CONCAT('%', LOWER(:keyword), '%'))")
    Page<TempleComment> findMyTempleCommentsByKeyword(@Param("memberNo") Long memberNo, @Param("keyword") String keyword, Pageable pageable);
}