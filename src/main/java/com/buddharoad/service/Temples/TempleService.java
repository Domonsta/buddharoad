package com.buddharoad.service.Temples;

import com.buddharoad.dto.Admin.AdminContentSearchFilterDTO;
import com.buddharoad.dto.Temples.Temple.TempleRegisterRequestDTO;
import com.buddharoad.dto.Temples.Temple.TempleResponseDTO;
import com.buddharoad.dto.Temples.Temple.TempleSearchFilterDTO;
import com.buddharoad.dto.Temples.Temple.TempleUpdateRequestDTO;
import com.buddharoad.security.Role;
import org.springframework.data.domain.Page;

public interface TempleService {
    TempleResponseDTO registerTemple(TempleRegisterRequestDTO requestDTO, Role role);

    // 💡 clientIp 대신 userId 파라미터 추가
    TempleResponseDTO getTempleDetails(Long templeId, String userId);

    Page<TempleResponseDTO> searchTemples(TempleSearchFilterDTO filterDTO);

    // 💡 updateTemple 메서드 시그니처 변경 (newFiles, newFileDescriptions 제거, DTO에 포함)
    TempleResponseDTO updateTemple(Long templeId, TempleUpdateRequestDTO requestDTO, Role role);

    void deleteTemple(Long templeId, Role role);

    void changeTempleActiveStatus(Long templeId, Boolean isActive, Role role);

    // --- ⭐ 관리자용 API 메서드 추가 ⭐ ---
    /**
     * 관리자용: 비활성화되거나 삭제된 사찰 목록을 검색하고 페이징 처리하여 반환한다.
     * @param filter 검색 필터 (keyword, page, size, sortBy, sortOrder 등 포함)
     * 사찰은 작성자 개념이 없으므로 memberUsername은 무시된다.
     * @return 페이징 처리된 TempleResponseDTO 목록
     */
    Page<TempleResponseDTO> searchInactiveTemplesForAdmin(AdminContentSearchFilterDTO filter);
}
