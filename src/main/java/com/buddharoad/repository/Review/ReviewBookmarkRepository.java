package com.buddharoad.repository.Review; // 💡 리뷰 관련 패키지 경로로 변경

import com.buddharoad.domain.ReviewBookmark;
import com.buddharoad.domain.ReviewBookmarkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewBookmarkRepository extends JpaRepository<ReviewBookmark, ReviewBookmarkId> {

    // ✅ 특정 회원이 특정 리뷰를 찜했는지 확인
    boolean existsByMemberNoAndReviewId(Long memberNo, Long reviewId);

    // ✅ 특정 회원이 찜한 모든 리뷰 목록 조회 (마이페이지용)
    List<ReviewBookmark> findByMemberNo(Long memberNo);

    // ✅ 토글을 위한 찜 정보 조회
    Optional<ReviewBookmark> findByMemberNoAndReviewId(Long memberNo, Long reviewId);

    // ✅ 특정 찜 정보 삭제
    @Transactional
    void deleteByMemberNoAndReviewId(Long memberNo, Long reviewId);
}