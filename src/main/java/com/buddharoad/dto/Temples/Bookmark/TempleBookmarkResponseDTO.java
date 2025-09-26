package com.buddharoad.dto.Temples.Bookmark;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter // Lombok 사용 시 주석 해제
@Setter // Lombok 사용 시 주석 해제
public class TempleBookmarkResponseDTO {

    private Long memberNo;
    private Long templeId;
    private String templeName; // 마이페이지에서 보여줄 사찰 이름
    private LocalDateTime createdAt;
    private Boolean bookmarked;

}