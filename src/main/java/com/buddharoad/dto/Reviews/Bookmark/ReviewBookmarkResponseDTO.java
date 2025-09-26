package com.buddharoad.dto.Reviews.Bookmark; // 패키지 경로 변경

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class ReviewBookmarkResponseDTO {

    private Long reviewId; // 찜한 리뷰의 ID
    private String reviewTitle; // 💡 리뷰 제목
    private Long memberNo; // 찜한 회원 번호
    private LocalDateTime createdAt;
    private Boolean isBookmarked; // 💡 찜 상태 (찜했는지/안했는지)

}