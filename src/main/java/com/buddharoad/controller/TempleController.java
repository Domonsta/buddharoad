package com.buddharoad.controller;

import com.buddharoad.dto.Temples.Temple.TempleRegisterRequestDTO;
import com.buddharoad.dto.Temples.Temple.TempleResponseDTO;
import com.buddharoad.dto.Temples.Temple.TempleSearchFilterDTO;
import com.buddharoad.dto.Temples.Temple.TempleUpdateRequestDTO;
import com.buddharoad.security.Role; // Role Enum 임포트
import com.buddharoad.service.Temples.TempleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2; // lombok.extern.log4j2로 변경
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // MediaType 임포트
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 권한 부여를 위해 추가
import org.springframework.security.core.Authentication; // 💡 Authentication 임포트 추가
import org.springframework.validation.BindingResult; // 유효성 검사 결과
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 파일 업로드를 위해 추가

import java.util.List;
import java.util.stream.Collectors; // List 처리용

@RestController
@RequestMapping("/api/temples")
@RequiredArgsConstructor
@Log4j2
public class TempleController {

    private final TempleService templeService;

    // 💡 사찰 등록 (POST)
    // - 권한: 콘텐츠 관리자(CONTENT_MANAGER), 시스템 관리자(SYSTEM_ADMIN)만 가능
    // - 파일 업로드가 포함되므로 consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> registerTemple(
            @RequestPart("request") @Valid TempleRegisterRequestDTO requestDTO, // JSON DTO를 "request" part로 받음
            BindingResult bindingResult,
            @RequestPart(value = "files", required = false) List<MultipartFile> files // 파일 리스트를 "files" part로 받음
    ) {
        log.info("📢 사찰 등록 요청: {}", requestDTO);
        log.info("📢 첨부된 파일 개수: {}", files != null ? files.size() : 0);

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.warn("🚨 사찰 등록 유효성 검사 실패: {}", errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }

        try {
            Role currentUserRole = Role.SYSTEM_ADMIN;

            requestDTO.setFiles(files);

            TempleResponseDTO registeredTemple = templeService.registerTemple(requestDTO, currentUserRole);
            log.info("✅ 사찰 등록 성공: {}", registeredTemple.getTempleName());
            return new ResponseEntity<>(registeredTemple, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("❌ 사찰 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사찰 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 특정 사찰 상세 조회 (GET)
    // - 권한: 모든 사용자 접근 가능
    @GetMapping("/{templeId}")
    public ResponseEntity<?> getTempleDetails(
            @PathVariable Long templeId,
            Authentication authentication // 💡 Authentication 객체 주입
    ) {
        log.info("🔎 사찰 상세 조회 요청: ID {}", templeId);
        try {
            // 로그인한 사용자의 ID (username) 가져오기
            String userId = authentication != null ? authentication.getName() : "anonymousUser"; // 💡 비회원 처리
            log.info("조회 요청 사용자 ID: {}", userId);

            TempleResponseDTO templeDetails = templeService.getTempleDetails(templeId, userId); // 💡 서비스에 userId 전달
            log.info("✅ 사찰 상세 조회 성공: {}", templeDetails.getTempleName());
            return ResponseEntity.ok(templeDetails);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            log.warn("⚠️ 사찰을 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 사찰 상세 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사찰 상세 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 사찰 목록 검색 및 페이징 (GET)
    // - 권한: 모든 사용자 접근 가능
    // - @ModelAttribute를 사용하여 쿼리 파라미터를 DTO에 바인딩
    @GetMapping("/search")
    public ResponseEntity<?> searchTemples(@ModelAttribute TempleSearchFilterDTO filterDTO) {
        log.info("🔍 사찰 목록 검색 요청: {}", filterDTO);
        try {
            Page<TempleResponseDTO> pageResult = templeService.searchTemples(filterDTO);
            log.info("✅ 사찰 목록 검색 성공. 총 {}개, 현재 페이지 {}/{}",
                    pageResult.getTotalElements(), pageResult.getNumber() + 1, pageResult.getTotalPages());
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("❌ 사찰 목록 검색 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사찰 목록 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 사찰 정보 수정 (PUT/PATCH)
    // - 권한: 콘텐츠 관리자(CONTENT_MANAGER), 시스템 관리자(SYSTEM_ADMIN)만 가능
    // - 파일 업로드가 포함될 수 있으므로 consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    @PutMapping(value = "/{templeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> updateTemple(
            @PathVariable Long templeId,
            @RequestPart("request") @Valid TempleUpdateRequestDTO requestDTO,
            BindingResult bindingResult,
            @RequestPart(value = "newPhotos", required = false) List<MultipartFile> newPhotos, // 💡 DTO 필드명과 일치하도록 변경
            @RequestPart(value = "newFileDescriptions", required = false) List<String> newFileDescriptions // 💡 DTO 필드명과 일치하도록 변경
    ) {
        log.info("🔄 사찰 수정 요청: ID {}", templeId);
        log.info("🔄 첨부된 새 파일 개수: {}", newPhotos != null ? newPhotos.size() : 0);
        log.info("🔄 첨부된 새 파일 설명 개수: {}", newFileDescriptions != null ? newFileDescriptions.size() : 0);


        // DTO에 파일과 설명 목록 설정
        requestDTO.setNewPhotos(newPhotos);
        requestDTO.setNewFileDescriptions(newFileDescriptions);

        log.info("🔄 삭제할 사진 ID 개수: {}", requestDTO.getDeletedPhotoIds() != null ? requestDTO.getDeletedPhotoIds().size() : 0);
        log.info("🔄 수정할 사진 정보 개수: {}", requestDTO.getUpdatedPhotoInfo() != null ? requestDTO.getUpdatedPhotoInfo().size() : 0);

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.warn("🚨 사찰 수정 유효성 검사 실패: {}", errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }

        try {
            Role currentUserRole = Role.SYSTEM_ADMIN;
            TempleResponseDTO updatedTemple = templeService.updateTemple(templeId, requestDTO, currentUserRole);
            log.info("✅ 사찰 수정 성공: {}", updatedTemple.getTempleName());
            return ResponseEntity.ok(updatedTemple);
        } catch (EntityNotFoundException e) {
            log.warn("⚠️ 사찰을 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 권한 없음 또는 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 사찰 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사찰 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    // 💡 사찰 소프트 삭제 (DELETE)
    // - 권한: 콘텐츠 관리자(CONTENT_MANAGER), 시스템 관리자(SYSTEM_ADMIN)만 가능
    @DeleteMapping("/{templeId}")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> deleteTemple(@PathVariable Long templeId) {
        log.info("🗑️ 사찰 삭제 요청: ID {}", templeId);
        try {
            Role currentUserRole = Role.SYSTEM_ADMIN;
            templeService.deleteTemple(templeId, currentUserRole);
            log.info("✅ 사찰 소프트 삭제 성공: ID {}", templeId);
            return ResponseEntity.ok("사찰이 성공적으로 삭제(비활성화)되었습니다.");
        } catch (jakarta.persistence.EntityNotFoundException e) {
            log.warn("⚠️ 사찰을 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403 Forbidden
        } catch (Exception e) {
            log.error("❌ 사찰 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사찰 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 💡 사찰 활성화/비활성화 상태 변경 (PATCH)
    // - 권한: 콘텐츠 관리자(CONTENT_MANAGER), 시스템 관리자(SYSTEM_ADMIN)만 가능
    @PatchMapping("/{templeId}/status")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<?> changeTempleActiveStatus(
            @PathVariable Long templeId,
            @RequestParam Boolean isActive
    ) {
        log.info("⚡️ 사찰 활성화 상태 변경 요청: ID {}, isActive: {}", templeId, isActive);
        try {
            Role currentUserRole = Role.SYSTEM_ADMIN;
            templeService.changeTempleActiveStatus(templeId, isActive, currentUserRole);
            log.info("✅ 사찰 활성화 상태 변경 성공: ID {}, isActive: {}", templeId, isActive);
            return ResponseEntity.ok("사찰 활성화 상태가 성공적으로 변경되었습니다.");
        } catch (jakarta.persistence.EntityNotFoundException e) {
            log.warn("⚠️ 사찰을 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403 Forbidden
        } catch (Exception e) {
            log.error("❌ 사찰 활성화 상태 변경 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사찰 활성화 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
