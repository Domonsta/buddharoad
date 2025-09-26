package com.buddharoad.service.Reviews;

import com.buddharoad.domain.Member;
import com.buddharoad.domain.Review;
import com.buddharoad.domain.ReviewPhoto;
import com.buddharoad.domain.Temple;
import com.buddharoad.dto.Admin.AdminContentSearchFilterDTO;
import com.buddharoad.dto.Reviews.Review.*;
import com.buddharoad.repository.Review.ReviewPhotoRepository;
import com.buddharoad.repository.Review.ReviewRepository;
import com.buddharoad.repository.Member.MemberRepository;
import com.buddharoad.repository.Temple.TempleRepository;
import com.buddharoad.security.Role;
import com.buddharoad.service.storage.StorageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate; // ⭐ 이 import를 사용해야 해!
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification; // ⭐ 이 import를 사용해야 해!
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList; // List를 위해
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final TempleRepository templeRepository;
    private final ReviewPhotoRepository reviewPhotoRepository;
    private final StorageService storageService;
    private final EntityManager entityManager;

    // ✨✨✨ 리뷰 조회수 중복 방지를 위한 인메모리 맵 (userId 기반) ✨✨✨
    // 서버 재시작 시 데이터가 초기화됩니다. 영구적인 저장이 필요하면 DB 테이블을 사용하거나 Redis를 연동해야 합니다.
    private final Map<String, Map<Long, LocalDateTime>> lastReviewViewAccessByUserId = new ConcurrentHashMap<>();
    private static final Duration REVIEW_VIEW_COOLDOWN = Duration.ofHours(24); // 💡 24시간 쿨다운 설정

    @Override
    @Transactional
    public ReviewResponseDTO registerReview(ReviewRegisterRequestDTO requestDTO, List<MultipartFile> files, Member currentMember) {
        log.info("📝 리뷰 등록 서비스 호출됨. 사찰 ID: {}, 사용자: {}", requestDTO.getTempleId(), currentMember.getDisplayName());

        Temple temple = templeRepository.findById(requestDTO.getTempleId())
                .orElseThrow(() -> {
                    log.error("❌ 사찰을 찾을 수 없음: ID {}", requestDTO.getTempleId());
                    return new EntityNotFoundException("해당 ID의 사찰을 찾을 수 없습니다: " + requestDTO.getTempleId());
                });

        Review review = requestDTO.toEntity(currentMember, temple);

        if (requestDTO.getReviewTags() != null && !requestDTO.getReviewTags().isEmpty()) {
            review.setTags(String.join(",", requestDTO.getReviewTags()));
        } else {
            review.setTags("");
        }

        reviewRepository.save(review);
        log.info("✅ 리뷰 기본 정보 저장 완료. Review ID: {}", review.getReviewId());

        if (files != null && !files.isEmpty()) {
            int order = 0;
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (!file.isEmpty()) {
                    try {
                        String description = (requestDTO.getFileDescriptions() != null && i < requestDTO.getFileDescriptions().size()) ?
                                requestDTO.getFileDescriptions().get(i) : "";
                        String photoUrl = storageService.uploadFile(file, description);
                        ReviewPhoto reviewPhoto = ReviewPhoto.builder()
                                .photoUrl(photoUrl)
                                .description(description)
                                .photoOrder(order++)
                                .review(review)
                                .build();
                        review.addPhoto(reviewPhoto);
                        log.info("⬆️ 사진 업로드 및 연관 설정 완료: {}", photoUrl);
                    } catch (Exception e) {
                        log.error("❌ 사진 업로드 실패: {}", e.getMessage(), e);
                    }
                }
            }
            reviewRepository.save(review);
            log.info("✅ 모든 사진 저장 및 리뷰 업데이트 완료.");
        }

        return ReviewResponseDTO.fromEntity(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> searchReviews(ReviewSearchFilterDTO filterDTO) {
        log.info("🔍 리뷰 검색 서비스 호출됨. 필터: {}", filterDTO);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (filterDTO.getSortBy() != null && !filterDTO.getSortBy().isEmpty()) {
            Sort.Direction direction = "asc".equalsIgnoreCase(filterDTO.getSortOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, filterDTO.getSortBy());
        }

        Pageable pageable = PageRequest.of(
                filterDTO.getPage() != null ? filterDTO.getPage() : 0,
                filterDTO.getSize() != null ? filterDTO.getSize() : 10,
                sort
        );

        Page<Review> reviewsPage = reviewRepository.searchReviews(filterDTO, pageable);

        return reviewsPage.map(ReviewResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public ReviewResponseDTO getReviewDetails(Long reviewId, String userLoginId) {
        // ✨ [로그 Service] getReviewDetails 메소드 진입 확인
        log.info(">>>> [Service] getReviewDetails 메소드 진입. reviewId: {}, userLoginId: {}", reviewId, userLoginId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    // ✨ [로그 Service] 첫 번째 findById 실패 확인
                    log.error(">>>> [Service] 첫 번째 reviewRepository.findById({}) 실패! EntityNotFoundException 발생.", reviewId);
                    return new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId);
                });

        // ✨ [로그 Service] 첫 번째 findById 성공 및 review 객체 정보 확인
        log.info(">>>> [Service] 첫 번째 reviewRepository.findById({}) 성공. review.isActive: {}, review.isDeleted: {}",
                reviewId, review.getIsActive(), review.getIsDeleted());


        if (!review.getIsActive() || review.getIsDeleted()) {
            // ✨ [로그 Service] 비활성화/삭제된 리뷰 조건 진입 확인
            log.warn(">>>> [Service] 비활성화/삭제된 리뷰 조건 진입! reviewId: {}, isActive: {}, isDeleted: {}",
                    reviewId, review.getIsActive(), review.getIsDeleted());
            if (userLoginId == null || !review.getMember().getLoginId().equals(userLoginId)) {
                // ✨ [로그 Service] 비활성화/삭제된 리뷰 & 작성자 불일치 조건 진입 확인
                log.warn(">>>> [Service] 비활성화/삭제된 리뷰이며, 작성자가 아님. userLoginId: {}", userLoginId);
                throw new IllegalArgumentException("비활성화되었거나 삭제된 리뷰는 접근할 수 없습니다.");
            }
        }

        // ✨✨✨ 조회수 중복 방지 로직 시작 (TempleCommentServiceImpl 로직 참고) ✨✨✨
        // userLoginId가 null이거나 빈 문자열인 경우 "anonymousUser"로 처리하여 쿨다운 적용
        String effectiveUserId = (userLoginId == null || userLoginId.trim().isEmpty()) ? "anonymousUser" : userLoginId;

        Map<Long, LocalDateTime> userViewMap = lastReviewViewAccessByUserId.computeIfAbsent(effectiveUserId, k -> new ConcurrentHashMap<>());

        if (userViewMap.containsKey(reviewId)) {
            LocalDateTime lastAccessed = userViewMap.get(reviewId);
            // 쿨다운 시간(24시간)이 지나지 않았다면 조회수 증가를 건너뜁니다.
            if (Duration.between(lastAccessed, LocalDateTime.now()).compareTo(REVIEW_VIEW_COOLDOWN) < 0) {
                log.info("🚫 리뷰 ID {}는 사용자 {}에 의해 쿨다운 시간 ({}) 내에 재접속하여 조회수 증가를 건너뛰며, DTO 변환 후 반환됩니다.", reviewId, effectiveUserId, REVIEW_VIEW_COOLDOWN.toMinutes() + "분");
                return ReviewResponseDTO.fromEntity(review); // 조회수 증가 없이 바로 반환
            }
        }

        // 쿨다운이 지났거나 첫 조회인 경우 조회수 증가 및 마지막 접근 시간 업데이트
        log.info(">>>> [Service] incrementViewCountById 호출 시작. 리뷰 ID: {}", reviewId);
        reviewRepository.incrementViewCountById(reviewId); // 직접 쿼리 호출
        log.info(">>>> [Service] incrementViewCountById 호출 완료. 리뷰 ID: {}", reviewId);

        log.info(">>>> [Service] entityManager.flush() 직전.");
        entityManager.flush(); // 현재까지의 변경사항을 DB에 즉시 동기화
        log.info(">>>> [Service] entityManager.clear() 직전.");
        entityManager.clear(); // 영속성 컨텍스트를 비워서 엔티티 캐시 제거

        // DTO에 최신 조회수를 반영하기 위해 DB에서 다시 조회
        log.info(">>>> [Service] 두 번째 reviewRepository.findById({}) 호출 직전.", reviewId);
        Review updatedReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    // ✨ [로그 Service] 두 번째 findById 실패 확인
                    log.error(">>>> [Service] 두 번째 reviewRepository.findById({}) 실패! EntityNotFoundException 발생.", reviewId);
                    return new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId);
                });

        // 조회수 증가 성공 후 맵 업데이트
        userViewMap.put(reviewId, LocalDateTime.now());

        // ✨ [로그 Service] 모든 로직 성공
        log.info(">>>> [Service] 모든 로직 성공. 최종 반환 전. updatedReview.viewCount: {}", updatedReview.getViewCount());
        return ReviewResponseDTO.fromEntity(updatedReview);
        // ✨✨✨ 조회수 중복 방지 로직 끝 ✨✨✨
    }


    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateRequestDTO requestDTO, List<MultipartFile> newFiles, List<Long> deletedPhotoIds, Member currentMember) {
        log.info("🔄 리뷰 수정 서비스 호출됨. 리뷰 ID: {}, 요청 사용자: {}", reviewId, currentMember.getDisplayName());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("❌ 수정할 리뷰를 찾을 수 없음: ID {}", reviewId);
                    return new EntityNotFoundException("수정할 리뷰를 찾을 수 없습니다: " + reviewId);
                });

        if (!review.getMember().getMemberNo().equals(currentMember.getMemberNo()) &&
                currentMember.getRole() != Role.CONTENT_ADMIN && currentMember.getRole() != Role.SYSTEM_ADMIN) {
            log.warn("🚨 리뷰 수정 권한 없음: 작성자 불일치 또는 권한 부족. 리뷰 작성자: {}, 요청자: {}, 역할: {}",
                    review.getMember().getMemberNo(), currentMember.getMemberNo(), currentMember.getRole());
            throw new IllegalArgumentException("리뷰를 수정할 권한이 없습니다.");
        }

        review.setTitle(requestDTO.getTitle());
        review.setContent(requestDTO.getContent());
        review.setRating(requestDTO.getRating());
        review.setTags(requestDTO.getTags());
        review.setVisitedAt(requestDTO.getVisitedAt());

        if (requestDTO.getUpdatedPhotoDescriptions() != null) {
            requestDTO.getUpdatedPhotoDescriptions().forEach(updatedPhotoDTO -> {
                review.getPhotos().stream()
                        .filter(photo -> photo.getPhotoId().equals(updatedPhotoDTO.getPhotoId()))
                        .findFirst()
                        .ifPresent(photo -> {
                            photo.updateDescription(updatedPhotoDTO.getDescription());
                        });
            });
        }

        if (deletedPhotoIds != null && !deletedPhotoIds.isEmpty()) {
            List<ReviewPhoto> photosToDelete = review.getPhotos().stream()
                    .filter(photo -> deletedPhotoIds.contains(photo.getPhotoId()))
                    .collect(Collectors.toList());

            photosToDelete.forEach(photo -> {
                storageService.deleteFile(photo.getPhotoUrl());
                review.removePhoto(photo);
                log.info("🗑️ 사진 삭제 완료: Photo ID {}", photo.getPhotoId());
            });
        }

        if (newFiles != null && !newFiles.isEmpty()) {
            int currentMaxOrder = review.getPhotos().stream()
                    .mapToInt(ReviewPhoto::getPhotoOrder)
                    .max()
                    .orElse(-1);

            for (int i = 0; i < newFiles.size(); i++) {
                MultipartFile file = newFiles.get(i);
                if (!file.isEmpty()) {
                    try {
                        String description = (requestDTO.getNewPhotoDescriptions() != null && i < requestDTO.getNewPhotoDescriptions().size()) ?
                                requestDTO.getNewPhotoDescriptions().get(i) : "";
                        String photoUrl = storageService.uploadFile(file, description);
                        ReviewPhoto newPhoto = ReviewPhoto.builder()
                                .photoUrl(photoUrl)
                                .description(description)
                                .photoOrder(currentMaxOrder + 1 + i)
                                .review(review)
                                .build();
                        review.addPhoto(newPhoto);
                        log.info("⬆️ 새로운 사진 업로드 및 추가 완료: {}", photoUrl);
                    } catch (Exception e) {
                        log.error("❌ 새로운 사진 업로드 실패: {}", e.getMessage(), e);
                    }
                }
            }
        }

        reviewRepository.save(review);
        log.info("✅ 리뷰 수정 완료. Review ID: {}", review.getReviewId());

        return ReviewResponseDTO.fromEntity(review);
    }

    @Override
    @Transactional
    public void softDeleteReview(Long reviewId, Member currentMember) {
        log.info("🗑️ 리뷰 소프트 삭제 서비스 호출됨. 리뷰 ID: {}, 요청 회원 번호: {}", reviewId, currentMember.getMemberNo());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("❌ 삭제할 리뷰를 찾을 수 없음: ID {}", reviewId);
                    return new EntityNotFoundException("삭제할 리뷰를 찾을 수 없습니다: " + reviewId);
                });

        if (!review.getMember().getMemberNo().equals(currentMember.getMemberNo()) &&
                currentMember.getRole() != Role.CONTENT_ADMIN && currentMember.getRole() != Role.SYSTEM_ADMIN) {
            log.warn("🚨 리뷰 삭제 권한 없음: 작성자 불일치 또는 권한 부족. 리뷰 작성자: {}, 요청자: {}, 역할: {}",
                    review.getMember().getMemberNo(), currentMember.getMemberNo(), currentMember.getRole());
            throw new IllegalArgumentException("리뷰를 삭제할 권한이 없습니다.");
        }

        if (review.getIsDeleted()) {
            log.warn("이미 삭제된 리뷰입니다: ID {}", reviewId);
            throw new IllegalArgumentException("이미 삭제된 리뷰입니다.");
        }

        review.getPhotos().forEach(photo -> storageService.deleteFile(photo.getPhotoUrl()));

        review.softDelete();
        reviewRepository.save(review);
        log.info("✅ 리뷰 소프트 삭제 성공: ID {}", reviewId);
    }

    @Override
    @Transactional
    public void changeReviewActiveStatus(Long reviewId, Boolean isActive, Role role) {
        log.info("⚡️ 리뷰 활성화 상태 변경 서비스 호출됨. ID: {}, isActive: {}, 권한: {}", reviewId, isActive, role);

        if (role != Role.CONTENT_ADMIN && role != Role.SYSTEM_ADMIN) {
            log.warn("리뷰 활성화 상태 변경 권한 없음: 사용자 역할 {}", role);
            throw new IllegalArgumentException("리뷰 활성화 상태 변경 권한이 없습니다.");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("리뷰를 찾을 수 없음: ID {}", reviewId);
                    return new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId);
                });

        if (review.getIsDeleted()) {
            log.warn("삭제된 리뷰의 활성화 상태 변경 시도: ID {}", reviewId);
            throw new IllegalArgumentException("삭제된 리뷰는 상태를 변경할 수 없습니다.");
        }

        review.setIsActive(isActive);
        reviewRepository.save(review);
        log.info("✅ 리뷰 활성화 상태 변경 성공: ID={}, isActive: {}", reviewId, isActive);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getMyReviews(Long memberNo, ReviewSearchFilterDTO filterDTO) {
        log.info("🔎 내 리뷰 목록 조회 서비스 호출됨. 회원 번호: {}, 필터: {}", memberNo, filterDTO);

        // 정렬 기준 및 순서 설정
        Sort sort = Sort.by(filterDTO.getSortDirection(), filterDTO.getSortBy());
        Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize(), sort);

        // Specification을 사용하여 동적 쿼리 생성
        Specification<Review> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 💡 항상 현재 로그인된 회원의 리뷰만 조회
            predicates.add(cb.equal(root.get("member").get("memberNo"), memberNo));

            // 💡 삭제되지 않은 리뷰만 조회
            predicates.add(cb.isFalse(root.get("isDeleted")));
            // 💡 활성 상태인 리뷰만 조회 (관리자가 비활성화한 것도 제외)
            predicates.add(cb.isTrue(root.get("isActive")));

            // 제목/내용 검색 (keyword 필드 활용)
            // ⭐ 'getSearchKeyword()' 대신 'getKeyword()' 사용!
            if (filterDTO.getKeyword() != null && !filterDTO.getKeyword().isBlank()) {
                String keyword = "%" + filterDTO.getKeyword().toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), keyword);
                Predicate contentPredicate = cb.like(cb.lower(root.get("content")), keyword);
                predicates.add(cb.or(titlePredicate, contentPredicate));
            }

            if (filterDTO.getMinViewCount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("viewCount"), filterDTO.getMinViewCount()));
            }
            if (filterDTO.getMaxViewCount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("viewCount"), filterDTO.getMaxViewCount()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // ⭐ reviewRepository가 JpaSpecificationExecutor를 상속받았는지 확인 필수!
        // (이전에 확인했지만, 다시 한번 확인해 줘)
        Page<Review> reviewPage = reviewRepository.findAll(spec, pageable);

        Page<ReviewResponseDTO> dtoPage = reviewPage.map(ReviewResponseDTO::fromEntity);
        log.info("✅ 내 리뷰 목록 조회 성공: 총 {}개 리뷰, {} 페이지", dtoPage.getTotalElements(), dtoPage.getNumber());
        return dtoPage;
        }

    // --- ⭐ 관리자용 API 메서드 구현 시작 ⭐ ---
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> searchInactiveReviewsForAdmin(AdminContentSearchFilterDTO filter) {
        log.info("🔎 ReviewService: 관리자용 비활성화/삭제된 리뷰 조회 시작. 필터: {}", filter);

        Sort sort = Sort.by(filter.getSortDirection(), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Review> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // isDeleted=true 또는 isActive=false인 리뷰만 조회
            predicates.add(cb.or(
                    cb.isTrue(root.get("isDeleted")),
                    cb.isFalse(root.get("isActive"))
            ));

            // 키워드 검색 (제목 또는 내용)
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), keyword);
                Predicate contentPredicate = cb.like(cb.lower(root.get("content")), keyword);
                predicates.add(cb.or(titlePredicate, contentPredicate));
            }

            // 작성자 닉네임 검색
            if (filter.getMemberUsername() != null && !filter.getMemberUsername().isBlank()) {
                // Member 엔티티와 조인하여 memberUsername 필드 검색
                // Review 엔티티에 memberUsername이 직접 필드로 있는 경우 아래처럼
                // predicates.add(cb.like(cb.lower(root.get("memberUsername")), "%" + filter.getMemberUsername().toLowerCase() + "%"));
                // Review 엔티티에 Member 엔티티와 연관 관계가 있다면 아래처럼
                predicates.add(cb.like(cb.lower(root.get("member").get("username")), "%" + filter.getMemberUsername().toLowerCase() + "%"));
                // Review 엔티티에 "memberUsername" 필드가 직접 있어서 그걸 쓰는거면 위 코드를 주석처리하고 아래 코드를 쓰면 돼.
                // predicates.add(cb.like(cb.lower(root.get("memberUsername")), "%" + filter.getMemberUsername().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // ReviewRepository가 JpaSpecificationExecutor를 상속받았는지 확인 필수!
        // (import org.springframework.data.jpa.domain.Specification; 도 확인)
        Page<Review> reviewsPage = reviewRepository.findAll(spec, pageable);
        log.info("✅ 관리자용 비활성화/삭제된 리뷰 검색 결과: 총 {}개, 현재 페이지 {}/{}",
                reviewsPage.getTotalElements(), reviewsPage.getNumber() + 1, reviewsPage.getTotalPages());
        return reviewsPage.map(ReviewResponseDTO::fromEntity);
    }
    // --- ⭐ 관리자용 API 메서드 구현 끝 ⭐ ---
}