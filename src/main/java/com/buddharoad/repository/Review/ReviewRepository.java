// src/main/java/com/buddharoad/repository/Review/ReviewRepository.java
package com.buddharoad.repository.Review;

import com.buddharoad.domain.Review;
import com.buddharoad.dto.Reviews.Review.ReviewSearchFilterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    // 💡 추가: 특정 리뷰 ID로 삭제되지 않은 리뷰를 조회하는 메서드
    Optional<Review> findByReviewIdAndIsDeletedFalse(Long reviewId);

    Page<Review> findByIsDeletedFalse(Pageable pageable);
    Page<Review> findByTemple_TempleIdAndIsDeletedFalse(Long templeId, Pageable pageable);
    Page<Review> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title, Pageable pageable);
    Page<Review> findByContentContainingIgnoreCaseAndIsDeletedFalse(String content, Pageable pageable);
    Page<Review> findByMemberMemberNoAndIsDeletedFalse(Long memberNo, Pageable pageable);

    @Query("SELECT r FROM Review r " +
            "LEFT JOIN r.member m " +
            "LEFT JOIN r.temple t WHERE " +
            "(:#{#filter.title} IS NULL OR r.title LIKE CONCAT('%', :#{#filter.title}, '%') OR r.content LIKE CONCAT('%', :#{#filter.title}, '%')) AND " +
            "(:#{#filter.tags} IS NULL OR r.tags LIKE CONCAT('%', :#{#filter.tags}, '%')) AND " +
            "(:#{#filter.minRating} IS NULL OR r.rating >= :#{#filter.minRating}) AND " +
            "(:#{#filter.maxRating} IS NULL OR r.rating <= :#{#filter.maxRating}) AND " +
            "(:#{#filter.templeId} IS NULL OR r.temple.templeId = :#{#filter.templeId}) AND " + // ⭐ 이 부분은 이미 수정되어 있었어!
            "(:#{#filter.memberNo} IS NULL OR r.member.memberNo = :#{#filter.memberNo}) AND " +
            "(:#{#filter.memberUsername} IS NULL OR m.username LIKE CONCAT('%', :#{#filter.memberUsername}, '%')) AND " +
            "(:#{#filter.templeName} IS NULL OR t.templeName LIKE CONCAT('%', :#{#filter.templeName}, '%')) AND " +
            "(:#{#filter.region} IS NULL OR t.region = :#{#filter.region}) AND " +
            "(:#{#filter.isActive} IS NULL OR r.isActive = :#{#filter.isActive}) AND r.isDeleted = false")
    Page<Review> searchReviews(@Param("filter") ReviewSearchFilterDTO filter, Pageable pageable);

    // ⭐ 이 searchByFilter 쿼리를 수정해야 해! ⭐
    @Query("SELECT r FROM Review r LEFT JOIN r.member m LEFT JOIN r.temple t WHERE " + // t를 조인해야 t.templeName에 접근 가능!
            "( :keyword IS NULL OR r.title LIKE CONCAT('%', :keyword, '%') OR r.content LIKE CONCAT('%', :keyword, '%') ) AND " +
            "( :tags IS NULL OR r.tags LIKE CONCAT('%', :tags, '%') ) AND " +
            "( :ratingMin IS NULL OR r.rating >= :ratingMin ) AND " +
            "( :ratingMax IS NULL OR r.rating <= :ratingMax ) AND " +
            "( :templeId IS NULL OR r.temple.templeId = :templeId ) AND " + // ⭐⭐ 여기가 핵심 수정 포인트! ⭐⭐
            "( :memberNo IS NULL OR r.member.memberNo = :memberNo ) AND " +
            "r.isDeleted = false AND r.isActive = true")
    Page<Review> searchByFilter(
            @Param("keyword") String keyword,
            @Param("tags") String tags,
            @Param("ratingMin") Integer ratingMin,
            @Param("ratingMax") Integer ratingMax,
            @Param("templeId") Long templeId,
            @Param("memberNo") Long memberNo,
            Pageable pageable);

    @Modifying // UPDATE, DELETE 쿼리에 필수
    @Query("UPDATE Review r SET r.viewCount = r.viewCount + 1 WHERE r.reviewId = :reviewId")
    void incrementViewCountById(@Param("reviewId") Long reviewId);
}