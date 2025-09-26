package com.buddharoad.service.Reviews; // 💡 패키지 경로를 review로 변경

import com.buddharoad.domain.Member;
import com.buddharoad.domain.Review; // 💡 Review 엔티티 임포트
import com.buddharoad.domain.ReviewBookmark; // 💡 ReviewBookmark 엔티티 임포트
import com.buddharoad.dto.Reviews.Bookmark.ReviewBookmarkResponseDTO; // 💡 리뷰용 DTO 임포트
import com.buddharoad.repository.Member.MemberRepository;
import com.buddharoad.repository.Review.ReviewBookmarkRepository; // 💡 리뷰용 리포지토리 임포트
import com.buddharoad.repository.Review.ReviewRepository; // 💡 리뷰용 리포지토리 임포트
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ReviewBookmarkService {

    private final ReviewBookmarkRepository reviewBookmarkRepository; // 💡 리뷰 리포지토리로 변경
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository; // 💡 리뷰 리포지토리로 변경

    /**
     * 리뷰 찜하기 상태를 토글(추가/삭제)합니다.
     * @param memberNo 현재 로그인한 회원의 ID
     * @param reviewId 찜할 리뷰의 ID
     * @return 찜하기 상태 (true: 찜함, false: 찜하지 않음)
     */
    public boolean toggleBookmark(Long memberNo, Long reviewId) {
        // 회원과 리뷰 엔티티를 조회
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberNo));
        Review review = reviewRepository.findById(reviewId) // 💡 Review 엔티티 조회
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));

        Optional<ReviewBookmark> existingBookmark = reviewBookmarkRepository.findByMemberNoAndReviewId(memberNo, reviewId);

        if (existingBookmark.isPresent()) {
            log.info("🗑️ 리뷰 찜하기 삭제: 회원 {}번, 리뷰 {}번", memberNo, reviewId);
            reviewBookmarkRepository.deleteByMemberNoAndReviewId(memberNo, reviewId);
            return false;
        } else {
            log.info("❤️ 리뷰 찜하기 추가: 회원 {}번, 리뷰 {}번", memberNo, reviewId);
            ReviewBookmark bookmark = new ReviewBookmark();
            bookmark.setMember(member);
            bookmark.setReview(review); // 💡 Review 엔티티 설정
            bookmark.setCreatedAt(LocalDateTime.now());
            reviewBookmarkRepository.save(bookmark);
            return true;
        }
    }

    /**
     * 💡 특정 리뷰의 찜하기 상태를 조회합니다.
     * @param memberNo 현재 로그인한 회원의 ID
     * @param reviewId 확인할 리뷰의 ID
     * @return 찜하기 상태 (true: 찜함, false: 찜하지 않음)
     */
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long memberNo, Long reviewId) {
        log.info("🔍 리뷰 찜하기 상태 조회: 회원 {}번, 리뷰 {}번", memberNo, reviewId);
        return reviewBookmarkRepository.existsByMemberNoAndReviewId(memberNo, reviewId);
    }

    /**
     * 💡 특정 회원이 찜한 모든 리뷰 목록을 조회합니다.
     * @param memberNo 현재 로그인한 회원의 ID
     * @return 찜한 리뷰 목록 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ReviewBookmarkResponseDTO> getBookmarksByMemberNo(Long memberNo) {
        log.info("📜 회원 {}번의 찜 목록 조회", memberNo);
        List<ReviewBookmark> bookmarks = reviewBookmarkRepository.findByMemberNo(memberNo);

        return bookmarks.stream()
                .map(bookmark -> {
                    // Review 엔티티 정보로 DTO 빌드
                    return ReviewBookmarkResponseDTO.builder()
                            .reviewId(bookmark.getReviewId())
                            .reviewTitle(bookmark.getReview().getTitle()) // 💡 리뷰 제목도 추가
                            .memberNo(bookmark.getMemberNo())
                            .createdAt(bookmark.getCreatedAt())
                            .isBookmarked(true)
                            .build();
                })
                .collect(Collectors.toList());
    }
}