// src/main/java/com/buddharoad/repository/Reviews/ReviewPhotoRepository.java
package com.buddharoad.repository.Review;

import com.buddharoad.domain.ReviewPhoto;
import com.buddharoad.domain.Review; // Review 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional; // 추가

import java.util.List; // 추가

public interface ReviewPhotoRepository extends JpaRepository<ReviewPhoto, Long> {
    List<ReviewPhoto> findByReview(Review review); // 특정 리뷰의 사진 목록 조회

    @Transactional // 트랜잭션 내에서 실행되도록
    void deleteAllByReview(Review review); // 특정 리뷰의 모든 사진 삭제
}