package com.buddharoad.service.Admin;

import com.buddharoad.dto.Admin.AdminContentSearchFilterDTO;
import com.buddharoad.dto.Admin.AdminStatisticsResponseDTO;
import com.buddharoad.dto.Reviews.Review.ReviewResponseDTO;
import com.buddharoad.dto.Reviews.Comment.ReviewCommentResponseDTO;
import com.buddharoad.dto.Temples.Temple.TempleResponseDTO;
import com.buddharoad.dto.Temples.Comment.TempleCommentResponseDTO;
import com.buddharoad.repository.Member.MemberRepository;
import com.buddharoad.repository.Review.ReviewCommentRepository;
import com.buddharoad.repository.Review.ReviewRepository;
import com.buddharoad.repository.Temple.TempleCommentRepository;
import com.buddharoad.repository.Temple.TempleRepository;
import com.buddharoad.service.Reviews.ReviewCommentService; // ReviewCommentService 임포트
import com.buddharoad.service.Reviews.ReviewService; // ReviewService 임포트
import com.buddharoad.service.Temples.TempleCommentService; // TempleCommentService 임포트
import com.buddharoad.service.Temples.TempleService; // TempleService 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page; // Page 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AdminServiceImpl implements AdminService { // AdminService 인터페이스 구현

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final TempleCommentRepository templeCommentRepository;
    private final TempleRepository templeRepository;

    // 다른 서비스들을 주입받아 사용
    private final TempleService templeService;
    private final ReviewService reviewService;
    private final TempleCommentService templeCommentService;
    private final ReviewCommentService reviewCommentService;


    @Override // @Override 어노테이션 추가
    @Transactional(readOnly = true)
    public AdminStatisticsResponseDTO getSiteStatistics() {
        log.info("📊 사이트 통계 데이터 조회 시작...");

        Long totalMembers = memberRepository.count();
        Long totalReviews = reviewRepository.count();
        Long totalTemples = templeRepository.count();
        Long totalPosts = totalReviews + totalTemples;

        Long totalReviewComments = reviewCommentRepository.count();
        Long totalTempleComments = templeCommentRepository.count();
        Long totalComments = totalReviewComments + totalTempleComments;

        log.info("✅ 사이트 통계 데이터 조회 성공: 회원 수 {}, 게시글 수 {}, 댓글 수 {}", totalMembers, totalPosts, totalComments);

        return AdminStatisticsResponseDTO.builder()
                .totalMembers(totalMembers)
                .totalPosts(totalPosts) // ⭐ 수정: totalTemples 대신 totalPosts
                .totalComments(totalComments) // ⭐ 수정: totalReviewComments 대신 totalComments
                .build();
    }

    @Override // @Override 어노테이션 추가
    @Transactional(readOnly = true)
    public Page<TempleResponseDTO> getInactiveTemples(AdminContentSearchFilterDTO filter) {
        log.info("📊 AdminService: 비활성화된 사찰 조회 시작. 필터: {}", filter);
        // ⭐ TempleService에 `searchInactiveTemplesForAdmin` 메서드를 추가해야 함
        // 이 메서드는 `AdminContentSearchFilterDTO`를 받아서 isDeleted=true 또는 isActive=false인 사찰을 검색하도록 구현될 예정
        return templeService.searchInactiveTemplesForAdmin(filter);
    }

    @Override // @Override 어노테이션 추가
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getInactiveReviews(AdminContentSearchFilterDTO filter) {
        log.info("📊 AdminService: 비활성화된 리뷰 조회 시작. 필터: {}", filter);
        // ⭐ ReviewService에 `searchInactiveReviewsForAdmin` 메서드를 추가해야 함
        // 이 메서드는 `AdminContentSearchFilterDTO`를 받아서 isDeleted=true 또는 isActive=false,
        // 그리고 memberUsername으로 검색하도록 구현될 예정
        return reviewService.searchInactiveReviewsForAdmin(filter);
    }

    @Override // @Override 어노테이션 추가
    @Transactional(readOnly = true)
    public Page<TempleCommentResponseDTO> getInactiveTempleComments(AdminContentSearchFilterDTO filter) {
        log.info("📊 AdminService: 비활성화된 사찰 댓글 조회 시작. 필터: {}", filter);
        // ⭐ TempleCommentService에 `searchInactiveTempleCommentsForAdmin` 메서드를 추가해야 함
        // 이 메서드는 `AdminContentSearchFilterDTO`를 받아서 isDeleted=true,
        // 그리고 memberUsername으로 검색하도록 구현될 예정
        return templeCommentService.searchInactiveTempleCommentsForAdmin(filter);
    }

    @Override // @Override 어노테이션 추가
    @Transactional(readOnly = true)
    public Page<ReviewCommentResponseDTO> getInactiveReviewComments(AdminContentSearchFilterDTO filter) {
        log.info("📊 AdminService: 비활성화된 리뷰 댓글 조회 시작. 필터: {}", filter);
        // ⭐ ReviewCommentService에 `searchInactiveReviewCommentsForAdmin` 메서드를 추가해야 함
        // 이 메서드는 `AdminContentSearchFilterDTO`를 받아서 isDeleted=true,
        // 그리고 memberUsername으로 검색하도록 구현될 예정
        return reviewCommentService.searchInactiveReviewCommentsForAdmin(filter);
    }
}