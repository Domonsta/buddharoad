// src/main/java/com/buddharoad/repository/Review/ReviewCommentRepository.java
package com.buddharoad.repository.Review;

import com.buddharoad.domain.ReviewComment;
import com.buddharoad.domain.ReviewCommentId; // 💡 복합 키 클래스 임포트
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
// 💡 기본 키 타입(PK)을 ReviewCommentId로 변경!
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, ReviewCommentId>, JpaSpecificationExecutor<ReviewComment> {

    // 1. 특정 댓글을 조회하는 메서드 (단일 조회)
    // 💡 복합 키를 사용하므로 findByReviewCommentId는 사용 불가
    // 리뷰 ID와 회원 번호로 유일한 댓글을 찾아야 함
    Optional<ReviewComment> findByReviewReviewIdAndMemberMemberNoAndIsDeletedFalse(Long reviewId, Long memberNo);

    // 2. 특정 리뷰(Review)에 달린 활성 댓글을 페이징하여 조회하는 메서드
    Page<ReviewComment> findByReviewReviewIdAndIsDeletedFalse(Long reviewId, Pageable pageable);

    // 3. 특정 리뷰에 대한 활성 댓글 중 내용(content)을 포함하는 댓글을 페이징하여 조회
    Page<ReviewComment> findByReviewReviewIdAndContentContainingIgnoreCaseAndIsDeletedFalse(Long reviewId, String content, Pageable pageable);

    // 4. 특정 리뷰의 활성 댓글 총 개수를 조회하는 메서드
    long countByReviewReviewIdAndIsDeletedFalse(Long reviewId);

    // 5. 특정 회원이 작성한 모든 리뷰 댓글을 조회하는 메서드 (List)
    List<ReviewComment> findByMemberMemberNo(Long memberNo);

    // 6. 조회수 필드를 삭제했으므로 이 메서드도 삭제
    // Page<ReviewComment> findByReviewReviewIdAndIsDeletedFalseOrderByViewCountDesc(Long reviewId, Pageable pageable);

    // 7. 좋아요 수(likeCount) 기준으로 정렬된 특정 리뷰의 활성 댓글을 페이징하여 조회
    @Query("SELECT rc FROM ReviewComment rc " +
            "WHERE rc.review.reviewId = :reviewId AND rc.isDeleted = FALSE " +
            "ORDER BY rc.likeCount DESC, rc.createdAt DESC")
    Page<ReviewComment> findByReviewIdOrderByLikeCountDesc(@Param("reviewId") Long reviewId, Pageable pageable);

    // 💡 참고: 기존 JPQL 쿼리는 `likeCount` 필드가 이미 존재하므로 JOIN이 필요 없음

    // 8. 특정 회원이 작성한 활성 댓글을 페이징하여 조회
    Page<ReviewComment> findByMemberMemberNoAndIsDeletedFalse(Long memberNo, Pageable pageable);

    // 9. 특정 회원이 작성한 활성 댓글 중 내용이나 원본 리뷰 제목으로 검색 (JPQL 사용)
    @Query("SELECT rc FROM ReviewComment rc JOIN rc.review r " +
            "WHERE rc.member.memberNo = :memberNo AND rc.isDeleted = FALSE AND rc.isActive = TRUE " +
            "AND (:keyword IS NULL OR LOWER(rc.content) LIKE CONCAT('%', LOWER(:keyword), '%') OR LOWER(r.title) LIKE CONCAT('%', LOWER(:keyword), '%'))")
    Page<ReviewComment> findMyReviewCommentsByKeyword(@Param("memberNo") Long memberNo, @Param("keyword") String keyword, Pageable pageable);

}