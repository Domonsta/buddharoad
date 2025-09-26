package com.buddharoad.dto.Reviews.Review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
// Region Enum 타입을 사용하기 위해 임포트 추가
import com.buddharoad.domain.Region; // 💡💡💡 이 라인을 추가해줘! 💡💡💡

import org.springframework.data.domain.Sort;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSearchFilterDTO {
    private Long templeId;
    private Long memberNo;
    private String memberUsername;
    private String title;
    private String content;
    private Integer minRating;
    private Integer maxRating;
    private String tags;
    private String keyword; // 제목 또는 내용 검색을 위한 키워드
    private Boolean isActive;
    private Boolean isDeleted;
    private String sortBy; // 정렬 기준 (예: "createdAt", "rating", "viewCount")
    private String sortOrder; // 정렬 순서 (예: "asc", "desc")
    private Integer page; // 페이지 번호
    private Integer size; // 페이지 당 개수
    private Integer ratingMin;
    private Integer ratingMax;
    private String templeName;
    private Region region; // 💡💡💡 String에서 Region 타입으로 변경! 💡💡💡

    // ✨✨✨ 여기에 이 두 필드를 추가해줘! ✨✨✨
    private Long minViewCount; // 최소 조회수
    private Long maxViewCount; // 최대 조회수

    // ✨✨✨ getSortDirection() 메서드 추가! ✨✨✨
    public Sort.Direction getSortDirection() {
        // 기본값 "desc"
        return "asc".equalsIgnoreCase(this.sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}