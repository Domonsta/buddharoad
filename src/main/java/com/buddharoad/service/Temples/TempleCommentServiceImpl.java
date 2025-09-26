// src/main/java/com/buddharoad/service/Temples/TempleCommentServiceImpl.java
package com.buddharoad.service.Temples; // 적절한 패키지 경로로 수정해주세요.

import com.buddharoad.domain.Member;
import com.buddharoad.domain.Temple;
import com.buddharoad.domain.TempleComment;
import com.buddharoad.dto.Temples.Comment.TempleCommentRegisterRequestDTO;
import com.buddharoad.dto.Temples.Comment.TempleCommentResponseDTO;
import com.buddharoad.dto.Temples.Comment.TempleCommentSearchFilterDTO;
import com.buddharoad.dto.Temples.Comment.TempleCommentUpdateRequestDTO;
import com.buddharoad.repository.Member.MemberRepository; // MemberRepository import
import com.buddharoad.repository.Temple.TempleCommentRepository; // TempleCommentRepository import
import com.buddharoad.repository.Temple.TempleRepository; // TempleRepository import
import com.buddharoad.security.Role;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification; // 동적 쿼리를 위해 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.buddharoad.dto.Admin.AdminContentSearchFilterDTO;



import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional; // Optional import
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Log4j2
public class TempleCommentServiceImpl implements TempleCommentService {

    private final TempleCommentRepository templeCommentRepository;
    private final MemberRepository memberRepository; // MemberRepository 주입
    private final TempleRepository templeRepository; // TempleRepository 주입

    // 💡 댓글 조회수 중복 방지를 위한 인메모리 맵 (userId 기반)
    // 서버 재시작 시 데이터가 초기화됩니다. 영구적인 저장이 필요하면 DB 테이블을 사용해야 합니다.
    private final Map<String, Map<Long, LocalDateTime>> lastCommentViewAccessByUserId = new ConcurrentHashMap<>();
    private static final Duration COMMENT_VIEW_COOLDOWN = Duration.ofHours(24); // 💡 24시간 쿨다운 설정

    @Override
    @Transactional
    public TempleCommentResponseDTO registerComment(TempleCommentRegisterRequestDTO requestDTO, Long memberNo, Role role) {
        log.info("📢 댓글 등록 서비스 호출됨. 사찰 ID: {}, 회원 번호: {}", requestDTO.getTempleId(), memberNo);

        // 💡 권한 확인: 일반 회원 이상만 댓글 작성 가능
        // 'ANONYMOUS' 대신 'GUEST' 역할 사용
        if (role == Role.GUEST) {
            log.warn("🚨 댓글 작성 권한 없음: 비회원은 댓글을 작성할 수 없습니다. 회원 번호: {}", memberNo);
            throw new IllegalArgumentException("댓글 작성 권한이 없습니다. 로그인 후 이용해주세요.");
        }

        // Member 엔티티 조회
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> {
                    log.error("❌ 회원을 찾을 수 없음: 회원 번호 {}", memberNo);
                    return new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberNo);
                });

        // Temple 엔티티 조회
        Temple temple = templeRepository.findById(requestDTO.getTempleId())
                .orElseThrow(() -> {
                    log.error("❌ 사찰을 찾을 수 없음: 사찰 ID {}", requestDTO.getTempleId());
                    return new EntityNotFoundException("사찰을 찾을 수 없습니다: " + requestDTO.getTempleId());
                });

        // TempleComment 엔티티 생성
        TempleComment templeComment = TempleComment.builder()
                .temple(temple)
                .member(member) // 💡 조회된 Member 엔티티 설정
                .content(requestDTO.getContent())
                .isActive(true)
                .isDeleted(false)
                .viewCount(0) // 새 댓글은 조회수 0으로 시작
                .build();

        TempleComment savedComment = templeCommentRepository.save(templeComment);
        log.info("✅ 댓글 등록 성공. 댓글 ID: {}", savedComment.getTempleCommentId());

        return TempleCommentResponseDTO.fromEntity(savedComment);
    }

    @Override
    @Transactional
    public TempleCommentResponseDTO getCommentDetails(Long templeCommentId, String userId) {
        log.info("🔎 댓글 상세 조회 서비스 호출됨. 댓글 ID: {}, User ID: {}", templeCommentId, userId);

        TempleComment templeComment = templeCommentRepository.findById(templeCommentId)
                .orElseThrow(() -> {
                    log.error("❌ 댓글을 찾을 수 없음: 댓글 ID {}", templeCommentId);
                    return new EntityNotFoundException("댓글을 찾을 수 없습니다: " + templeCommentId);
                });

        // 💡 조회수 중복 방지 로직 시작 (userId 기반)
        // userId가 null이거나 빈 문자열인 경우 "anonymousUser"로 처리하여 쿨다운 적용
        String effectiveUserId = (userId == null || userId.trim().isEmpty()) ? "anonymousUser" : userId;

        Map<Long, LocalDateTime> userViewMap = lastCommentViewAccessByUserId.computeIfAbsent(effectiveUserId, k -> new ConcurrentHashMap<>());

        if (userViewMap.containsKey(templeCommentId)) {
            LocalDateTime lastAccessed = userViewMap.get(templeCommentId);
            // 쿨다운 시간(24시간)이 지나지 않았다면 조회수 증가를 건너뜁니다.
            if (Duration.between(lastAccessed, LocalDateTime.now()).compareTo(COMMENT_VIEW_COOLDOWN) < 0) {
                log.info("댓글 ID {}는 사용자 {}에 의해 쿨다운 시간 ({}) 내에 재접속하여 조회수 증가를 건너뜁니다.", templeCommentId, effectiveUserId, COMMENT_VIEW_COOLDOWN.toMinutes() + "분");
                return TempleCommentResponseDTO.fromEntity(templeComment); // 조회수 증가 없이 바로 반환
            }
        }

        // 쿨다운이 지났거나 첫 조회인 경우 조회수 증가 및 마지막 접근 시간 업데이트
        templeComment.incrementViewCount();
        templeCommentRepository.save(templeComment); // 조회수 변경 저장
        userViewMap.put(templeCommentId, LocalDateTime.now());

        log.info("댓글 ID {}의 조회수가 {}로 증가했습니다. (User ID: {})", templeCommentId, templeComment.getViewCount(), effectiveUserId);
        // 💡 조회수 중복 방지 로직 끝

        log.info("✅ 댓글 상세 정보 불러옴: ID {}", templeCommentId);
        return TempleCommentResponseDTO.fromEntity(templeComment);
    }

    @Override
    @Transactional(readOnly = true) // 검색은 읽기 전용 트랜잭션
    public Page<TempleCommentResponseDTO> searchComments(Long templeId, TempleCommentSearchFilterDTO filterDTO, String userId) { // 💡 userId 파라미터 추가
        log.info("🔍 댓글 검색 서비스 호출됨. 사찰 ID: {}, 필터: {}, User ID: {}", templeId, filterDTO, userId);

        // 정렬 기준 설정
        Sort sort = Sort.by(filterDTO.getSortDirection(), filterDTO.getSortBy());
        Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize(), sort);

        // 동적 쿼리를 위한 Specification 생성
        Specification<TempleComment> spec = (root, query, criteriaBuilder) -> {
            // 기본 조건: 특정 사찰에 속하고 삭제되지 않은 댓글
            jakarta.persistence.criteria.Predicate predicate = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("temple").get("templeId"), templeId),
                    criteriaBuilder.isFalse(root.get("isDeleted"))
            );

            // 내용 검색 조건 추가
            if (filterDTO.getContent() != null && !filterDTO.getContent().trim().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), "%" + filterDTO.getContent().toLowerCase() + "%")
                );
            }

            // 최소 조회수 조건 추가
            if (filterDTO.getMinViewCount() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("viewCount"), filterDTO.getMinViewCount())
                );
            }

            // 최소 좋아요 수 조건 추가 (JPA Specification만으로는 JOIN 후 COUNT를 이용한 필터링/정렬이 복잡합니다.)
            // 현재는 minLikeCount 필터링은 제외하고, 정렬은 Repository 메서드를 활용하도록 합니다.
            // 만약 minLikeCount 필터링이 필요하다면, @Query를 사용하여 JPQL로 구현해야 합니다.

            return predicate;
        };

        Page<TempleComment> commentsPage;

        // 좋아요 순 정렬은 @Query를 사용해야 하므로, sortBy가 "likeCount"일 경우 별도 처리
        if ("likeCount".equalsIgnoreCase(filterDTO.getSortBy())) {
            if (Sort.Direction.DESC.equals(filterDTO.getSortDirection())) {
                commentsPage = templeCommentRepository.findByTempleIdOrderByLikeCountDesc(templeId, pageable);
            } else {
                commentsPage = templeCommentRepository.findByTempleIdOrderByLikeCountAsc(templeId, pageable);
            }
        } else {
            // 그 외 정렬 기준 (createdAt, viewCount, content 등)은 Specification과 Pageable로 처리
            commentsPage = templeCommentRepository.findAll(spec, pageable); // Specification을 사용하여 모든 필터 조건 적용
        }

        log.info("✅ 댓글 검색 결과: 총 {}개, 현재 페이지 {}/{}",
                commentsPage.getTotalElements(), commentsPage.getNumber() + 1, commentsPage.getTotalPages());
        return commentsPage.map(TempleCommentResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public TempleCommentResponseDTO updateComment(TempleCommentUpdateRequestDTO requestDTO, Long memberNo, Role role) {
        log.info("🔄 댓글 수정 서비스 호출됨. 댓글 ID: {}, 회원 번호: {}", requestDTO.getTempleCommentId(), memberNo);

        // 💡 권한 확인: 작성자 본인 또는 관리자만 수정 가능
        // findByTempleCommentIdAndIsDeletedFalse가 Optional을 반환하도록 수정되었으므로 .orElseThrow() 사용
        TempleComment templeComment = templeCommentRepository.findByTempleCommentIdAndIsDeletedFalse(requestDTO.getTempleCommentId())
                .orElseThrow(() -> {
                    log.error("❌ 수정할 댓글을 찾을 수 없음: 댓글 ID {}", requestDTO.getTempleCommentId());
                    return new EntityNotFoundException("수정할 댓글을 찾을 수 없습니다: " + requestDTO.getTempleCommentId());
                });

        // 댓글 작성자와 요청하는 회원이 다르면서, 관리자 권한도 없는 경우
        if (!templeComment.getMember().getMemberNo().equals(memberNo) &&
                role != Role.CONTENT_ADMIN && role != Role.SYSTEM_ADMIN) {
            log.warn("🚨 댓글 수정 권한 없음: 작성자 불일치 또는 권한 부족. 댓글 작성자: {}, 요청자: {}, 역할: {}",
                    templeComment.getMember().getMemberNo(), memberNo, role);
            throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다.");
        }

        // 내용 업데이트
        templeComment.updateContent(requestDTO.getContent());
        // @UpdateTimestamp가 자동으로 updatedAt을 업데이트하므로 수동 설정 제거
        // templeComment.setUpdatedAt(LocalDateTime.now());

        TempleComment updatedComment = templeCommentRepository.save(templeComment);
        log.info("✅ 댓글 수정 성공. 댓글 ID: {}", updatedComment.getTempleCommentId());

        return TempleCommentResponseDTO.fromEntity(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long templeCommentId, Long memberNo, Role role) {
        log.info("🗑️ 댓글 삭제 서비스 호출됨. 댓글 ID: {}, 회원 번호: {}", templeCommentId, memberNo);

        // 💡 권한 확인: 작성자 본인 또는 관리자만 삭제 가능
        // findByTempleCommentIdAndIsDeletedFalse가 Optional을 반환하도록 수정되었으므로 .orElseThrow() 사용
        TempleComment templeComment = templeCommentRepository.findByTempleCommentIdAndIsDeletedFalse(templeCommentId)
                .orElseThrow(() -> {
                    log.error("❌ 삭제할 댓글을 찾을 수 없음: 댓글 ID {}", templeCommentId);
                    return new EntityNotFoundException("삭제할 댓글을 찾을 수 없습니다: " + templeCommentId);
                });

        // 댓글 작성자와 요청하는 회원이 다르면서, 관리자 권한도 없는 경우
        if (!templeComment.getMember().getMemberNo().equals(memberNo) &&
                role != Role.CONTENT_ADMIN && role != Role.SYSTEM_ADMIN) {
            log.warn("🚨 댓글 삭제 권한 없음: 작성자 불일치 또는 권한 부족. 댓글 작성자: {}, 요청자: {}, 역할: {}",
                    templeComment.getMember().getMemberNo(), memberNo, role);
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }

        // 소프트 삭제 처리
        templeComment.markAsDeleted();
        // @UpdateTimestamp가 자동으로 updatedAt을 업데이트하므로 수동 설정 제거
        // templeComment.setUpdatedAt(LocalDateTime.now());
        templeCommentRepository.save(templeComment);

        log.info("✅ 댓글 소프트 삭제 성공. 댓글 ID: {}", templeCommentId);
    }

    // ⭐ 추가: 특정 회원이 작성한 사찰 댓글 목록 조회 (마이페이지용)
    @Override
    @Transactional(readOnly = true)
    public Page<TempleCommentResponseDTO> getMyTempleComments(Long memberNo, String keyword, int page, int size, String sortBy, String sortOrder) {
        log.info("🔎 내 사찰 댓글 목록 조회 서비스 호출됨. 회원 번호: {}, 검색어: {}", memberNo, keyword);

        Sort sort = Sort.by("asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TempleComment> commentPage;
        if (keyword != null && !keyword.isBlank()) {
            commentPage = templeCommentRepository.findMyTempleCommentsByKeyword(memberNo, keyword, pageable);
        } else {
            commentPage = templeCommentRepository.findByMemberMemberNoAndIsDeletedFalseAndIsActiveTrue(memberNo, pageable);
        }

        Page<TempleCommentResponseDTO> dtoPage = commentPage.map(TempleCommentResponseDTO::fromEntity);
        log.info("✅ 내 사찰 댓글 목록 조회 성공: 총 {}개 댓글, {} 페이지", dtoPage.getTotalElements(), dtoPage.getNumber());
        return dtoPage;
    }

    // --- ⭐ 관리자용 API 메서드 구현 시작 ⭐ ---
    @Override
    @Transactional(readOnly = true)
    public Page<TempleCommentResponseDTO> searchInactiveTempleCommentsForAdmin(AdminContentSearchFilterDTO filter) {
        log.info("🔎 TempleCommentService: 관리자용 삭제된 사찰 댓글 조회 시작. 필터: {}", filter);

        Sort sort = Sort.by(filter.getSortDirection(), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<TempleComment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // isDeleted=true 인 댓글만 조회
            predicates.add(cb.isTrue(root.get("isDeleted")));
            // TempleComment 엔티티에는 isActive 필드가 없으므로 isTrue(root.get("isActive")) 조건은 제외

            // 키워드 검색 (댓글 내용)
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("content")), keyword));
            }

            // 작성자 닉네임 검색
            if (filter.getMemberUsername() != null && !filter.getMemberUsername().isBlank()) {
                // Member 엔티티와 조인하여 member.username (또는 displayName) 필드 검색
                predicates.add(cb.like(cb.lower(root.get("member").get("username")), "%" + filter.getMemberUsername().toLowerCase() + "%"));
                // 만약 TempleComment 엔티티에 "memberUsername" 필드가 직접 있다면 위 코드를 주석처리하고 아래 코드를 쓰면 돼.
                // predicates.add(cb.like(cb.lower(root.get("memberUsername")), "%" + filter.getMemberUsername().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // TempleCommentRepository가 JpaSpecificationExecutor를 상속받았는지 확인 필수!
        Page<TempleComment> commentsPage = templeCommentRepository.findAll(spec, pageable);
        log.info("✅ 관리자용 삭제된 사찰 댓글 검색 결과: 총 {}개, 현재 페이지 {}/{}",
                commentsPage.getTotalElements(), commentsPage.getNumber() + 1, commentsPage.getTotalPages());
        return commentsPage.map(TempleCommentResponseDTO::fromEntity);
    }
    // --- ⭐ 관리자용 API 메서드 구현 끝 ⭐ ---

}