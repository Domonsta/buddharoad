// src/main/java/com/buddharoad/dto/Member/MemberSearchFilterDTO.java (새로 생성)
package com.buddharoad.dto.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort; // Sort.Direction을 위해 임포트

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSearchFilterDTO {
    private String searchType; // 검색 기준 (LOGIN_ID, USERNAME, EMAIL)
    private String keyword;    // 검색 키워드
    private String accountStatus; // 계정 상태 필터링 (ACTIVE, BLOCKED, DEACTIVATED 등)
    private String role; // 역할 필터링 (USER, CONTENT_ADMIN, SYSTEM_ADMIN)

    @Builder.Default
    private String sortBy = "createdAt"; // 기본 정렬: 가입일
    @Builder.Default
    private String sortOrder = "desc";   // 기본 정렬 순서: 내림차순 (최신순)
    @Builder.Default
    private int page = 0; // 기본 페이지 번호 (0부터 시작)
    @Builder.Default
    private int size = 10; // 기본 페이지 크기

    // 정렬 순서 유효성 검사 및 변환 (sortBy, sortOrder는 프론트에서 넘어옴)
    public Sort.Direction getSortDirection() {
        return "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}