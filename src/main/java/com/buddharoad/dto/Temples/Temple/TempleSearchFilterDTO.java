package com.buddharoad.dto.Temples.Temple;

import lombok.*;
import org.springframework.data.domain.Sort; // Sort.Direction을 위해 import

@Getter
@Setter // @ModelAttribute로 바인딩될 때 setter가 필요해.
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TempleSearchFilterDTO {
    private String region;
    private String feature;
    private String templeName;
    private String name;       // 사찰 이름 검색을 위한 'name' 필드 추가
    // private Integer minViewCount;
    // private Integer Integer maxViewCount;

    @Builder.Default // 💡💡💡 sortBy 기본값 설정 �💡💡
    private String sortBy = "createdAt"; // 기본 정렬 기준

    @Builder.Default // 💡💡💡 sortOrder 기본값 설정 💡💡💡
    private Sort.Direction sortOrder = Sort.Direction.DESC; // 기본 정렬 순서 (DESC)

    @Builder.Default // 💡💡💡 page 기본값 설정 💡💡💡
    private int page = 0; // 기본 페이지 (0부터 시작)

    @Builder.Default // 💡💡💡 size 기본값 설정 💡💡💡
    private int size = 10; // 기본 페이지당 항목 수
}