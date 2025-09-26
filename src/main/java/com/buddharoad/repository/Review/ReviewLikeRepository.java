package com.buddharoad.repository.Review;

import com.buddharoad.domain.ReviewLike;
import com.buddharoad.domain.ReviewLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, ReviewLikeId> {

    // 💡 특정 리뷰에 특정 회원이 좋아요를 눌렀는지 확인하는 메서드
    // 복합키를 사용하므로 reviewId와 memberNo를 파라미터로 받아서 조회
    Optional<ReviewLike> findByReviewIdAndMemberNo(Long reviewId, Long memberNo);

    // 💡 특정 리뷰의 좋아요 개수를 세는 메서드
    int countByReviewId(Long reviewId);

    // 💡 특정 리뷰에 특정 회원이 좋아요를 눌렀다면 삭제하는 메서드
    void deleteByReviewIdAndMemberNo(Long reviewId, Long memberNo);
}