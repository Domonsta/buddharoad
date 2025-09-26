// src/main/java/com/buddharoad/service/Reviews/ReviewCommentServiceImpl.java
package com.buddharoad.service.Reviews;

import com.buddharoad.domain.Member;
import com.buddharoad.domain.Review;
import com.buddharoad.domain.ReviewComment;
import com.buddharoad.domain.ReviewCommentId;
import com.buddharoad.dto.Admin.AdminContentSearchFilterDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentRegisterRequestDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentResponseDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentSearchFilterDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentUpdateRequestDTO;
import com.buddharoad.repository.Member.MemberRepository;
import com.buddharoad.repository.Review.ReviewCommentRepository;
import com.buddharoad.repository.Review.ReviewRepository;
import com.buddharoad.security.Role;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ReviewCommentServiceImpl implements ReviewCommentService {

    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public ReviewCommentResponseDTO registerComment(ReviewCommentRegisterRequestDTO requestDTO, Long memberNo, String memberUsername) {
        log.info("📝 리뷰 댓글 등록 서비스 호출됨. 리뷰 ID: {}, 회원 번호: {}", requestDTO.getReviewId(), memberNo);

        // 이미 해당 회원이 이 리뷰에 댓글을 작성했는지 확인
        reviewCommentRepository.findByReviewReviewIdAndMemberMemberNoAndIsDeletedFalse(requestDTO.getReviewId(), memberNo)
                .ifPresent(comment -> {
                    log.error("❌ 이미 댓글을 작성한 회원: 회원 번호 {}, 리뷰 ID {}", memberNo, requestDTO.getReviewId());
                    throw new IllegalArgumentException("이미 해당 리뷰에 댓글을 작성했습니다.");
                });

        Review review = reviewRepository.findById(requestDTO.getReviewId())
                .orElseThrow(() -> {
                    log.error("❌ 리뷰를 찾을 수 없음: ID {}", requestDTO.getReviewId());
                    return new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + requestDTO.getReviewId());
                });

        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> {
                    log.error("❌ 회원을 찾을 수 없음: 회원 번호 {}", memberNo);
                    return new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberNo);
                });

        ReviewComment reviewComment = ReviewComment.builder()
                .review(review)
                .member(member)
                .memberUsername(memberUsername)
                .content(requestDTO.getContent())
                .isActive(true)
                .isDeleted(false)
                .build();

        ReviewComment savedComment = reviewCommentRepository.save(reviewComment);
        log.info("✅ 리뷰 댓글 등록 성공: 리뷰 ID {}, 회원 번호 {}", review.getReviewId(), member.getMemberNo());

        return ReviewCommentResponseDTO.fromEntity(savedComment);
    }

    @Override
    public ReviewCommentResponseDTO getComment(Long reviewId, Long memberNo) {
        log.info("🔍 리뷰 댓글 조회 서비스 호출됨. 리뷰 ID: {}, 회원 번호: {}", reviewId, memberNo);

        // 💡 복합 키를 사용해 조회
        ReviewCommentId commentId = new ReviewCommentId(memberNo, reviewId);

        ReviewComment reviewComment = reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("❌ 조회할 리뷰 댓글을 찾을 수 없음: 리뷰 ID {}, 회원 번호 {}", reviewId, memberNo);
                    return new EntityNotFoundException("조회할 리뷰 댓글을 찾을 수 없습니다.");
                });

        if (reviewComment.getIsDeleted()) {
            throw new EntityNotFoundException("삭제된 댓글입니다.");
        }

        return ReviewCommentResponseDTO.fromEntity(reviewComment);
    }


    @Override
    @Transactional
    public ReviewCommentResponseDTO updateComment(ReviewCommentUpdateRequestDTO requestDTO, Long memberNo, Role role) {
        log.info("✏️ 리뷰 댓글 업데이트 서비스 호출됨. 리뷰 ID: {}, 회원 번호: {}", requestDTO.getReviewId(), memberNo);

        // 💡 복합 키를 사용해 조회
        ReviewCommentId commentId = new ReviewCommentId(memberNo, requestDTO.getReviewId());

        ReviewComment reviewComment = reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("❌ 업데이트할 리뷰 댓글을 찾을 수 없음: 리뷰 ID {}, 회원 번호 {}", requestDTO.getReviewId(), memberNo);
                    return new EntityNotFoundException("업데이트할 리뷰 댓글을 찾을 수 없습니다.");
                });

        if (reviewComment.getIsDeleted()) {
            throw new EntityNotFoundException("삭제된 댓글은 수정할 수 없습니다.");
        }

        // 💡 권한 검사: 작성자가 본인이거나, 관리자 권한이 있어야 함
        if (!reviewComment.getMember().getMemberNo().equals(memberNo) && role != Role.CONTENT_ADMIN && role != Role.SYSTEM_ADMIN) {
            log.warn("🚨 리뷰 댓글 수정 권한 없음: 작성자 불일치 또는 권한 부족. 댓글 작성자: {}, 요청자: {}, 역할: {}",
                    reviewComment.getMember().getMemberNo(), memberNo, role);
            throw new IllegalArgumentException("리뷰 댓글을 수정할 권한이 없습니다.");
        }

        reviewComment.updateContent(requestDTO.getContent());

        ReviewComment updatedComment = reviewCommentRepository.save(reviewComment);
        log.info("✅ 리뷰 댓글 업데이트 성공: 리뷰 ID {}, 회원 번호 {}", updatedComment.getReview().getReviewId(), updatedComment.getMember().getMemberNo());

        return ReviewCommentResponseDTO.fromEntity(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long reviewId, Long memberNo, Role role) {
        log.info("🗑️ 리뷰 댓글 삭제 서비스 호출됨. 리뷰 ID: {}, 회원 번호: {}", reviewId, memberNo);

        // 💡 복합 키를 사용해 조회
        ReviewCommentId commentId = new ReviewCommentId(memberNo, reviewId);

        ReviewComment reviewComment = reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("❌ 삭제할 리뷰 댓글을 찾을 수 없음: 리뷰 ID {}, 회원 번호 {}", reviewId, memberNo);
                    return new EntityNotFoundException("삭제할 리뷰 댓글을 찾을 수 없습니다.");
                });

        if (reviewComment.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 댓글입니다.");
        }

        if (!reviewComment.getMember().getMemberNo().equals(memberNo) &&
                role != Role.CONTENT_ADMIN && role != Role.SYSTEM_ADMIN) {
            log.warn("🚨 리뷰 댓글 삭제 권한 없음: 작성자 불일치 또는 권한 부족. 댓글 작성자: {}, 요청자: {}, 역할: {}",
                    reviewComment.getMember().getMemberNo(), memberNo, role);
            throw new IllegalArgumentException("리뷰 댓글을 삭제할 권한이 없습니다.");
        }

        reviewComment.markAsDeleted();
        reviewCommentRepository.save(reviewComment);
        log.info("✅ 리뷰 댓글 삭제(비활성화) 성공: 리뷰 ID {}, 회원 번호 {}", reviewId, memberNo);
    }

    @Override
    public Page<ReviewCommentResponseDTO> searchComments(ReviewCommentSearchFilterDTO filter) {
        log.info("🔎 리뷰 댓글 검색 서비스 호출됨. 필터: {}", filter);

        Sort sort = Sort.by(filter.getSortDirection(), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<ReviewComment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getReviewId() != null) {
                predicates.add(cb.equal(root.get("review").get("reviewId"), filter.getReviewId()));
            } else {
                throw new IllegalArgumentException("리뷰 ID는 댓글 검색을 위해 필수입니다.");
            }

            if (filter.getContent() != null && !filter.getContent().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("content")), "%" + filter.getContent().toLowerCase() + "%"));
            }

            // 💡 조회수 관련 로직은 엔티티에서 제거되었으므로, 이 부분도 삭제!

            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.isTrue(root.get("isActive")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ReviewComment> commentPage = reviewCommentRepository.findAll(spec, pageable);

        Page<ReviewCommentResponseDTO> dtoPage = commentPage.map(ReviewCommentResponseDTO::fromEntity);
        log.info("✅ 리뷰 댓글 검색 성공. 총 {}개 댓글 중 {} 페이지 조회.", dtoPage.getTotalElements(), dtoPage.getNumber());

        return dtoPage;
    }

    @Override
    public long countActiveCommentsByReviewId(Long reviewId) {
        log.info("📊 특정 리뷰의 활성 댓글 수 조회 서비스 호출됨. 리뷰 ID: {}", reviewId);
        long count = reviewCommentRepository.countByReviewReviewIdAndIsDeletedFalse(reviewId);
        log.info("✅ 리뷰 ID {} 에 대한 활성 댓글 수: {}", reviewId, count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewCommentResponseDTO> getMyReviewComments(Long memberNo, String keyword, int page, int size, String sortBy, String sortOrder) {
        log.info("🔎 내 리뷰 댓글 목록 조회 서비스 호출됨. 회원 번호: {}, 검색어: {}", memberNo, keyword);

        Sort sort = Sort.by("asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ReviewComment> commentPage;
        if (keyword != null && !keyword.isBlank()) {
            commentPage = reviewCommentRepository.findMyReviewCommentsByKeyword(memberNo, keyword, pageable);
        } else {
            commentPage = reviewCommentRepository.findByMemberMemberNoAndIsDeletedFalse(memberNo, pageable);
        }

        Page<ReviewCommentResponseDTO> dtoPage = commentPage.map(ReviewCommentResponseDTO::fromEntity);
        log.info("✅ 내 리뷰 댓글 목록 조회 성공: 총 {}개 댓글, {} 페이지", dtoPage.getTotalElements(), dtoPage.getNumber());
        return dtoPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewCommentResponseDTO> searchInactiveReviewCommentsForAdmin(AdminContentSearchFilterDTO filter) {
        log.info("🔎 ReviewCommentService: 관리자용 삭제된 리뷰 댓글 조회 시작. 필터: {}", filter);

        Sort sort = Sort.by(filter.getSortDirection(), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<ReviewComment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isDeleted")));

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("content")), keyword));
            }

            if (filter.getMemberUsername() != null && !filter.getMemberUsername().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("memberUsername")), "%" + filter.getMemberUsername().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ReviewComment> commentsPage = reviewCommentRepository.findAll(spec, pageable);
        log.info("✅ 관리자용 삭제된 리뷰 댓글 검색 결과: 총 {}개, 현재 페이지 {}/{}",
                commentsPage.getTotalElements(), commentsPage.getNumber() + 1, commentsPage.getTotalPages());
        return commentsPage.map(ReviewCommentResponseDTO::fromEntity);
    }
}