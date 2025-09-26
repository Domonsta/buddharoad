package com.buddharoad.dto.Temples.Comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempleCommentSearchFilterDTO {

    private Long templeId; // 특정 사찰의 댓글만 검색할 경우 사용

    private String content; // 댓글 내용 검색

    // 조회수 및 좋아요 수 검색은 "이상", "이하" 등의 범위를 추가할 수 있습니다.
    // 여기서는 간단하게 특정 값 이상으로 가정합니다.
    private Integer minViewCount; // 최소 조회수
    private Integer minLikeCount; // 최소 좋아요 수

    // 정렬 기준
    private String sortBy = "createdAt"; // 기본 정렬: 작성일
    private String sortOrder = "desc";   // 기본 정렬 순서: 내림차순 (최신순)

    // 페이징
    private int page = 0; // 기본 페이지 번호 (0부터 시작)
    private int size = 10; // 기본 페이지 크기

    // 정렬 순서 유효성 검사 및 변환
    public Sort.Direction getSortDirection() {
        return "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}
