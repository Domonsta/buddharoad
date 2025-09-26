package com.buddharoad.service.Reviews;
import com.buddharoad.domain.Member;
import com.buddharoad.domain.Review;
import com.buddharoad.domain.ReviewLike;
import com.buddharoad.dto.Reviews.Likes.ReviewLikeResponseDTO;
import com.buddharoad.repository.Member.MemberRepository;
import com.buddharoad.repository.Review.ReviewLikeRepository;
import com.buddharoad.repository.Review.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReviewLikeResponseDTO toggleLike(Long reviewId, Long memberNo) {

        // 1. 좋아요를 누를 리뷰와 회원을 찾기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 2. 이미 좋아요를 눌렀는지 확인
        boolean isLiked;
        if (reviewLikeRepository.findByReviewIdAndMemberNo(reviewId, memberNo).isPresent()) {
            // 이미 좋아요를 누른 상태 -> 좋아요 취소 (DELETE)
            reviewLikeRepository.deleteByReviewIdAndMemberNo(reviewId, memberNo);

            // Review 엔티티의 좋아요 개수 감소
            review.decrementLikeCount();
            isLiked = false;
        } else {
            // 좋아요를 누르지 않은 상태 -> 좋아요 추가 (INSERT)
            ReviewLike reviewLike = ReviewLike.builder()
                    .reviewId(reviewId)
                    .memberNo(memberNo)
                    .review(review) // 연관관계 매핑
                    .member(member) // 연관관계 매핑
                    .build();

            reviewLikeRepository.save(reviewLike);

            // Review 엔티티의 좋아요 개수 증가
            review.incrementLikeCount();
            isLiked = true;
        }

        // 3. 좋아요 개수 재계산 및 DTO 반환
        Long updatedLikeCount = (long) reviewLikeRepository.countByReviewId(reviewId);

        return new ReviewLikeResponseDTO(true, isLiked, updatedLikeCount);
    }

    // ⭐⭐ 좋아요 개수를 가져오는 메서드 ⭐⭐
    public Long getLikeCount(Long reviewId) {
        return (long) reviewLikeRepository.countByReviewId(reviewId);
    }

    // ⭐⭐ 좋아요 상태를 확인하는 메서드 ⭐⭐
    public boolean checkIfLiked(Long reviewId, Long memberNo) {
        return reviewLikeRepository.findByReviewIdAndMemberNo(reviewId, memberNo).isPresent();
    }

}