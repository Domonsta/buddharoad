package com.buddharoad.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminContentSearchFilterDTO {
    private String boardType; // "TEMPLE", "REVIEW", "TEMPLE_COMMENT", "REVIEW_COMMENT"
    private String keyword;   // 제목/내용 검색 키워드
    private String memberUsername; // 작성자 닉네임 검색

    // 페이징 및 정렬
    private int page = 0; // 기본 페이지 번호 (0부터 시작)
    private int size = 10; // 기본 페이지 크기
    private String sortBy = "createdAt"; // 기본 정렬: 작성일
    private String sortOrder = "desc";   // 기본 정렬 순서: 내림차순 (최신순)

    // 정렬 순서 유효성 검사 및 변환
    public Sort.Direction getSortDirection() {
        return "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}