// src/main/java/com/buddharoad/service/Reviews/ReviewCommentLikeService.java
package com.buddharoad.service.Reviews;

import com.buddharoad.domain.*;
import com.buddharoad.dto.Reviews.Likes.ReviewCommentLikeResponseDTO;
import com.buddharoad.repository.Member.MemberRepository;
import com.buddharoad.repository.Review.ReviewCommentLikeRepository;
import com.buddharoad.repository.Review.ReviewCommentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReviewCommentLikeService {

    private final ReviewCommentLikeRepository reviewCommentLikeRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReviewCommentLikeResponseDTO toggleLike(Long reviewId, Long reviewCommentMemberNo, Long memberNo) {
        log.info("👍 댓글 좋아요 토글 요청: 리뷰 ID={}, 댓글 작성자 회원 번호={}, 좋아요 누르는 회원 번호={}",
                reviewId, reviewCommentMemberNo, memberNo);

        // 1. 댓글 및 회원 조회
        ReviewCommentId reviewCommentId = new ReviewCommentId(reviewId, reviewCommentMemberNo);

        ReviewComment reviewComment = reviewCommentRepository.findById(reviewCommentId)
                .orElseThrow(() -> {
                    log.error("❌ 댓글을 찾을 수 없음: 리뷰 ID={}, 댓글 작성자 회원 번호={}", reviewId, reviewCommentMemberNo);
                    return new EntityNotFoundException("댓글을 찾을 수 없습니다.");
                });

        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> {
                    log.error("❌ 회원을 찾을 수 없음: 회원 번호={}", memberNo);
                    return new EntityNotFoundException("회원을 찾을 수 없습니다.");
                });

        // 2. 좋아요 토글
        ReviewCommentLikeId likeId = new ReviewCommentLikeId(
                memberNo,
                reviewCommentId.getReviewId(),
                reviewCommentId.getMemberNo()
        );


        Optional<ReviewCommentLike> existingLike = reviewCommentLikeRepository.findById(likeId);

        boolean isLiked;
        if (existingLike.isPresent()) {
            reviewCommentLikeRepository.delete(existingLike.get());
            reviewComment.decrementLikeCount();
            isLiked = false;
            log.info("👎 좋아요 취소 성공. 댓글 좋아요 수 감소: {}", reviewComment.getLikeCount());
        } else {
            ReviewCommentLike reviewCommentLike = ReviewCommentLike.builder()
                    .id(likeId)
                    .reviewComment(reviewComment)
                    .member(member)
                    .build();

            reviewCommentLikeRepository.save(reviewCommentLike);
            reviewComment.incrementLikeCount();
            isLiked = true;
            log.info("👍 좋아요 추가 성공. 댓글 좋아요 수 증가: {}", reviewComment.getLikeCount());
        }

        return new ReviewCommentLikeResponseDTO(true, isLiked, reviewComment.getLikeCount());
    }


    // getLikeCount, checkIfLiked 메서드도 마찬가지로 ReviewCommentLikeId 객체를 생성해서 사용하도록 수정
    public Long getLikeCount(Long reviewId, Long reviewCommentMemberNo) {
        log.info("📊 댓글 좋아요 수 조회 요청: 리뷰 ID={}, 댓글 작성자 회원 번호={}", reviewId, reviewCommentMemberNo);

        ReviewComment reviewComment = reviewCommentRepository.findById(
                        new ReviewCommentId(reviewId, reviewCommentMemberNo)
                )
                .orElseThrow(() -> {
                    log.error("❌ 댓글을 찾을 수 없음: 리뷰 ID={}, 댓글 작성자 회원 번호={}", reviewId, reviewCommentMemberNo);
                    return new EntityNotFoundException("댓글을 찾을 수 없습니다.");
                });

        return reviewComment.getLikeCount();
    }

    public boolean checkIfLiked(Long reviewId, Long reviewCommentMemberNo, Long memberNo) {
        log.info("✅ 댓글 좋아요 여부 확인: 리뷰 ID={}, 댓글 작성자 회원 번호={}, 회원 번호={}", reviewId, reviewCommentMemberNo, memberNo);

        ReviewCommentId reviewCommentId = new ReviewCommentId(reviewId, reviewCommentMemberNo);
        ReviewCommentLikeId likeId = new ReviewCommentLikeId(
                memberNo,
                reviewCommentId.getReviewId(),
                reviewCommentId.getMemberNo()
        );

        return reviewCommentLikeRepository.findById(likeId).isPresent();
    }
}