// src/main/java/com/buddharoad/repository/Review/ReviewCommentLikeRepository.java
package com.buddharoad.repository.Review;

import com.buddharoad.domain.ReviewCommentLike;
import com.buddharoad.domain.ReviewCommentLikeId; // 💡 변경된 복합키 클래스 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// 💡 기본 키 타입을 ReviewCommentLikeId로 변경!
public interface ReviewCommentLikeRepository extends JpaRepository<ReviewCommentLike, ReviewCommentLikeId> {
    // 💡 @EmbeddedId를 사용하면 기본 메서드로 충분!
    // findByReviewCommentAndMember 같은 메서드는 필요시 추가
}