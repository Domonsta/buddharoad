package com.buddharoad.service.Temples;

import com.buddharoad.domain.Temple;
import com.buddharoad.domain.TemplePhoto;
import com.buddharoad.dto.Admin.AdminContentSearchFilterDTO;
import com.buddharoad.dto.Temples.Temple.TemplePhotoUpdateDTO;
import com.buddharoad.dto.Temples.Temple.TempleRegisterRequestDTO;
import com.buddharoad.dto.Temples.Temple.TempleResponseDTO;
import com.buddharoad.dto.Temples.Temple.TempleSearchFilterDTO;
import com.buddharoad.dto.Temples.Temple.TempleUpdateRequestDTO;
import com.buddharoad.repository.Temple.TemplePhotoRepository;
import com.buddharoad.repository.Temple.TempleRepository;
import com.buddharoad.security.Role;
import com.buddharoad.service.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;


@Service
@RequiredArgsConstructor
@Log4j2
public class TempleServiceImpl implements TempleService {

    private final TempleRepository templeRepository;
    private final TemplePhotoRepository templePhotoRepository;
    private final StorageService storageService;

    private static final String TEMPLE_PHOTOS_DIRECTORY = "temple-photos/";

    private final Map<String, Map<Long, LocalDateTime>> lastViewAccessByUserId = new ConcurrentHashMap<>();
    private static final Duration VIEW_COOLDOWN = Duration.ofHours(24);

    @Override
    @Transactional
    public TempleResponseDTO registerTemple(TempleRegisterRequestDTO requestDTO, Role role) {
        log.info("📢 사찰 등록 서비스 호출됨.");
        if (role != Role.CONTENT_ADMIN && role != Role.SYSTEM_ADMIN) {
            log.warn("사찰 등록 권한 없음: 사용자 역할 {}", role);
            throw new IllegalArgumentException("사찰 등록 권한이 없습니다.");
        }

        Temple temple = Temple.builder()
                .templeName(requestDTO.getTempleName())
                .feature(requestDTO.getFeature())
                .region(requestDTO.getRegion())
                .phoneNumber(requestDTO.getPhoneNumber())
                .homepage(requestDTO.getHomepage())
                .address(requestDTO.getAddress())
                .operatingHours(requestDTO.getOperatingHours())
                .holidays(requestDTO.getHolidays())
                .parkingInfo(requestDTO.getParkingInfo())
                .admissionFee(requestDTO.getAdmissionFee())
                .restroomInfo(requestDTO.getRestroomInfo())
                .accessibility(requestDTO.getAccessibility())
                .culturalAssets(requestDTO.getCulturalAssets())
                .description(requestDTO.getDescription())
                .isActive(true)
                .isDeleted(false)
                .viewCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        log.info("새 사찰 엔티티 생성: {}", temple.getTempleName());

        if (requestDTO.getFiles() != null && !requestDTO.getFiles().isEmpty()) {
            log.info("새로운 등록 파일 {}개 처리 시작.", requestDTO.getFiles().size());
            int photoOrder = 0;

            List<String> fileDescriptions = requestDTO.getFileDescriptions();

            for (int i = 0; i < requestDTO.getFiles().size(); i++) {
                MultipartFile file = requestDTO.getFiles().get(i);
                String description = (fileDescriptions != null && i < fileDescriptions.size()) ? fileDescriptions.get(i) : file.getOriginalFilename();

                String photoUrl = storageService.uploadFile(file, TEMPLE_PHOTOS_DIRECTORY);
                TemplePhoto photo = TemplePhoto.builder()
                        .photoUrl(photoUrl)
                        .description(description)
                        .photoOrder(photoOrder++)
                        .build();
                temple.addPhoto(photo);
                log.info("등록 파일 저장 및 추가: {} (설명: {})", photoUrl, description);
            }
            log.info("새로운 등록 파일 처리 완료.");
        } else {
            log.info("등록할 새로운 파일 없음.");
        }

        Temple savedTemple = templeRepository.save(temple);
        log.info("사찰 등록 및 DB 저장 완료. ID: {}", savedTemple.getTempleId());
        return TempleResponseDTO.fromEntity(savedTemple);
    }

    @Override
    @Transactional
    public TempleResponseDTO getTempleDetails(Long templeId, String userId) {
        log.info("🔎 사찰 상세 조회 서비스 호출됨. ID: {}, User ID: {}", templeId, userId);
        Temple temple = templeRepository.findById(templeId)
                .orElseThrow(() -> {
                    log.error("사찰을 찾을 수 없음: ID {}", templeId);
                    return new EntityNotFoundException("사찰을 찾을 수 없습니다: " + templeId);
                });

        if (userId != null && !userId.equals("anonymousUser")) {
            Map<Long, LocalDateTime> userViewMap = lastViewAccessByUserId.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());

            if (userViewMap.containsKey(templeId)) {
                LocalDateTime lastAccessed = userViewMap.get(templeId);
                if (Duration.between(lastAccessed, LocalDateTime.now()).compareTo(VIEW_COOLDOWN) < 0) {
                    log.info("사찰 ID {}는 사용자 {}에 의해 쿨다운 시간 ({}) 내에 재접속하여 조회수 증가를 건너꿉니다.", templeId, userId, VIEW_COOLDOWN.toMinutes() + "분");
                    return TempleResponseDTO.fromEntity(temple);
                }
            }
            log.info("🚨 비회원 조회수 증가 직전! 현재 조회수: {}", temple.getViewCount());
            temple.increaseViewCount();
            log.info("✅ 비회원 조회수 증가 완료! 변경 후 조회수: {}", temple.getViewCount());
            templeRepository.save(temple);
            log.info("💾 비회원 조회수 DB 저장 완료!");
            log.info("사찰 ID {}의 조회수가 {}로 증가했습니다. (User ID: {})", templeId, temple.getViewCount(), userId);
            userViewMap.put(templeId, LocalDateTime.now());

            log.info("사찰 ID {}의 조회수가 {}로 증가했습니다. (User ID: {})", templeId, temple.getViewCount(), userId);
        } else {
            temple.increaseViewCount();
            templeRepository.save(temple);
            log.info("사찰 ID {}의 조회수가 {}로 증가했습니다. (비회원 조회)", templeId, temple.getViewCount());
        }

        log.info("사찰 상세 정보 불러옴: {}", temple.getTempleName());
        return TempleResponseDTO.fromEntity(temple);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TempleResponseDTO> searchTemples(TempleSearchFilterDTO filterDTO) {
        log.info("🔍 사찰 검색 서비스 호출됨. 필터: {}", filterDTO);

        // 💡💡💡 filterDTO.getSortOrder()가 이미 Sort.Direction 타입이므로 바로 사용 💡💡💡
        Sort sort = Sort.by(filterDTO.getSortOrder(), filterDTO.getSortBy());

        Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize(), sort);

        String searchTempleName = filterDTO.getName() != null && !filterDTO.getName().isEmpty()
                ? filterDTO.getName()
                : filterDTO.getTempleName();

        String feature = filterDTO.getFeature();
        List<String> regionNamesList = null;

        if (filterDTO.getRegion() != null && !filterDTO.getRegion().trim().isEmpty()) {
            regionNamesList = Collections.singletonList(filterDTO.getRegion().toUpperCase());
            log.info("Region filter applied: {}", regionNamesList);
        } else {
            log.info("Region filter is empty or null, skipping region filtering.");
        }

        log.info("Calling templeRepository.findByFiltersWithMultipleRegions with:");
        log.info("  - templeName: {}", searchTempleName);
        log.info("  - feature: {}", feature);
        log.info("  - regionNamesList: {}", regionNamesList);
        log.info("  - pageable: {}", pageable);

        Page<Temple> templesPage = templeRepository.findByFiltersWithMultipleRegions(
                searchTempleName,
                feature,
                regionNamesList,
                pageable
        );
        log.info("사찰 검색 결과: 총 {}개, 현재 페이지 {}/{}",
                templesPage.getTotalElements(), templesPage.getNumber() + 1, templesPage.getTotalPages());
        return templesPage.map(TempleResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public TempleResponseDTO updateTemple(Long templeId, TempleUpdateRequestDTO requestDTO, Role role) {
        log.info("💡 사찰 수정 서비스 호출됨. templeId: {}", templeId);
        log.info("requestDTO: {}", requestDTO);
        log.info("newPhotos 개수: {}", requestDTO.getNewPhotos() != null ? requestDTO.getNewPhotos().size() : 0);
        log.info("newFileDescriptions 개수: {}", requestDTO.getNewFileDescriptions() != null ? requestDTO.getNewFileDescriptions().size() : 0);


        if (role != Role.CONTENT_ADMIN && role != Role.SYSTEM_ADMIN) {
            log.warn("사찰 수정 권한 없음: 사용자 역할 {}", role);
            throw new IllegalArgumentException("사찰 수정 권한이 없습니다.");
        }

        Temple temple = templeRepository.findById(templeId)
                .orElseThrow(() -> {
                    log.error("사찰을 찾을 수 없음: ID {}", templeId);
                    return new EntityNotFoundException("사찰을 찾을 수 없습니다: " + templeId);
                });
        log.info("기존 사찰 정보 불러옴: {}", temple.getTempleName());

        if (requestDTO.getIsActive() != null) {
            temple.setIsActive(requestDTO.getIsActive());
            log.info("isActive 상태 업데이트: {}", requestDTO.getIsActive());
        }

        // 기본 정보 업데이트
        temple.setTempleName(requestDTO.getTempleName());
        temple.setFeature(requestDTO.getFeature());
        temple.setRegion(requestDTO.getRegion());
        temple.setPhoneNumber(requestDTO.getPhoneNumber());
        temple.setHomepage(requestDTO.getHomepage());
        temple.setAddress(requestDTO.getAddress());
        temple.setOperatingHours(requestDTO.getOperatingHours());
        temple.setHolidays(requestDTO.getHolidays());
        temple.setParkingInfo(requestDTO.getParkingInfo());
        temple.setAdmissionFee(requestDTO.getAdmissionFee());
        temple.setRestroomInfo(requestDTO.getRestroomInfo());
        temple.setAccessibility(requestDTO.getAccessibility());
        temple.setCulturalAssets(requestDTO.getCulturalAssets());
        temple.setDescription(requestDTO.getDescription());
        temple.setUpdatedAt(LocalDateTime.now());
        log.info("기본 사찰 정보 업데이트 완료.");

        // 기존 사진 삭제 처리
        if (requestDTO.getDeletedPhotoIds() != null && !requestDTO.getDeletedPhotoIds().isEmpty()) {
            log.info("삭제할 사진 ID: {}", requestDTO.getDeletedPhotoIds());
            List<TemplePhoto> photosToDelete = templePhotoRepository.findAllById(requestDTO.getDeletedPhotoIds());
            for (TemplePhoto photo : photosToDelete) {
                storageService.deleteFile(photo.getPhotoUrl());
                temple.removePhoto(photo);
                log.info("사진 삭제: {}", photo.getPhotoUrl());
            }
            templePhotoRepository.deleteAll(photosToDelete);
            log.info("기존 사진 삭제 처리 완료.");
        }

        // 새로운 사진 추가 처리 (파일과 설명 매칭)
        List<MultipartFile> newPhotos = requestDTO.getNewPhotos();
        List<String> newPhotoDescriptions = requestDTO.getNewFileDescriptions();

        if (newPhotos != null && !newPhotos.isEmpty()) {
            log.info("새로운 사진 {}개 추가 시작.", newPhotos.size());

            Integer maxPhotoOrder = temple.getPhotos().stream()
                    .map(TemplePhoto::getPhotoOrder)
                    .filter(java.util.Objects::nonNull)
                    .max(java.util.Comparator.naturalOrder())
                    .orElse(-1);

            int currentPhotoOrder = maxPhotoOrder + 1;

            for (int i = 0; i < newPhotos.size(); i++) {
                MultipartFile file = newPhotos.get(i);
                String description = (newPhotoDescriptions != null && i < newPhotoDescriptions.size()) ? newPhotoDescriptions.get(i) : file.getOriginalFilename();

                String photoUrl = storageService.uploadFile(file, TEMPLE_PHOTOS_DIRECTORY);
                TemplePhoto newPhoto = TemplePhoto.builder()
                        .photoUrl(photoUrl)
                        .description(description)
                        .photoOrder(currentPhotoOrder++)
                        .build();
                temple.addPhoto(newPhoto);
                log.info("새 사진 저장 및 추가: {} (설명: {})", photoUrl, description);
            }
            log.info("새로운 사진 추가 처리 완료.");
        } else {
            log.info("새로운 사진 없음.");
        }

        // 기존 사진 정보 수정 처리
        if (requestDTO.getUpdatedPhotoInfo() != null && !requestDTO.getUpdatedPhotoInfo().isEmpty()) {
            log.info("수정할 사진 정보 {}개 처리 시작.", requestDTO.getUpdatedPhotoInfo().size());
            for (TemplePhotoUpdateDTO updatedPhoto : requestDTO.getUpdatedPhotoInfo()) {
                temple.getPhotos().stream()
                        .filter(p -> p.getPhotoId().equals(updatedPhoto.getPhotoId()))
                        .findFirst()
                        .ifPresent(p -> {
                            p.setDescription(updatedPhoto.getDescription());
                            log.info("사진 ID {} 설명 업데이트: {}", p.getPhotoId(), updatedPhoto.getDescription());
                        });
            }
            log.info("기존 사진 정보 수정 처리 완료.");
        } else {
            log.info("수정할 사진 정보 없음.");
        }

        Temple updatedTemple = templeRepository.save(temple);
        log.info("사찰 정보 DB 저장 완료. ID: {}", updatedTemple.getTempleId());
        return TempleResponseDTO.fromEntity(updatedTemple);
    }

    @Override
    @Transactional
    public void deleteTemple(Long templeId, Role role) {
        log.info("🗑️ 사찰 삭제 서비스 호출됨. ID: {}", templeId);
        if (role != Role.CONTENT_ADMIN && role != Role.SYSTEM_ADMIN) {
            log.warn("사찰 삭제 권한 없음: 사용자 역할 {}", role);
            throw new IllegalArgumentException("사찰 삭제 권한이 없습니다.");
        }
        Temple temple = templeRepository.findById(templeId)
                .orElseThrow(() -> {
                    log.error("사찰을 찾을 수 없음: ID {}", templeId);
                    return new EntityNotFoundException("사찰을 찾을 수 없습니다: " + templeId);
                });
        temple.setIsDeleted(true);
        temple.setUpdatedAt(LocalDateTime.now());
        templeRepository.save(temple);
        log.info("사찰 소프트 삭제 성공: ID {}", templeId);
    }

    @Override
    @Transactional
    public void changeTempleActiveStatus(Long templeId, Boolean isActive, Role role) {
        log.info("⚡️ 사찰 활성화 상태 변경 서비스 호출됨. ID: {}, isActive: {}", templeId, isActive);
        if (role != Role.CONTENT_ADMIN && role != Role.SYSTEM_ADMIN) {
            log.warn("사찰 활성화 상태 변경 권한 없음: 사용자 역할 {}", role);
            throw new IllegalArgumentException("사찰 활성화 상태 변경 권한이 없습니다.");
        }
        Temple temple = templeRepository.findById(templeId)
                .orElseThrow(() -> {
                    log.error("사찰을 찾을 수 없음: ID {}", templeId);
                    return new EntityNotFoundException("사찰을 찾을 수 없습니다: " + templeId);
                });
        temple.setIsActive(isActive);
        temple.setUpdatedAt(LocalDateTime.now());
        templeRepository.save(temple);
        log.info("사찰 활성화 상태 변경 성공: ID {}, isActive: {}", templeId, isActive);
    }

    // --- ⭐ 관리자용 API 메서드 구현 시작 ⭐ ---
    @Override
    @Transactional(readOnly = true)
    public Page<TempleResponseDTO> searchInactiveTemplesForAdmin(AdminContentSearchFilterDTO filter) {
        log.info("🔎 TempleService: 관리자용 비활성화/삭제된 사찰 조회 시작. 필터: {}", filter);

        Sort sort = Sort.by(filter.getSortDirection(), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Temple> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // isDeleted=true 또는 isActive=false인 사찰만 조회
            predicates.add(cb.or(
                    cb.isTrue(root.get("isDeleted")),
                    cb.isFalse(root.get("isActive"))
            ));

            // 키워드 검색 (사찰 이름, 설명, 특징 등에서 검색 가능하도록 확장)
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("templeName")), keyword);
                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), keyword);
                Predicate featurePredicate = cb.like(cb.lower(root.get("feature")), keyword);
                // 사찰의 경우 memberUsername은 해당 없지만, 향후 확장성을 위해 주석 처리
                // Predicate memberUsernamePredicate = cb.like(cb.lower(root.get("member").get("displayName")), keyword);
                predicates.add(cb.or(namePredicate, descriptionPredicate, featurePredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Temple> templesPage = templeRepository.findAll(spec, pageable);
        log.info("✅ 관리자용 비활성화/삭제된 사찰 검색 결과: 총 {}개, 현재 페이지 {}/{}",
                templesPage.getTotalElements(), templesPage.getNumber() + 1, templesPage.getTotalPages());
        return templesPage.map(TempleResponseDTO::fromEntity);
    }
    // --- ⭐ 관리자용 API 메서드 구현 끝 ⭐ ---
}