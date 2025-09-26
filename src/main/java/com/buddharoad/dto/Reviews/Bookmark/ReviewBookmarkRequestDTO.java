package com.buddharoad.dto.Reviews.Bookmark; // 패키지 경로 변경

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewBookmarkRequestDTO {

    @NotNull(message = "리뷰 ID는 필수입니다.")
    private Long reviewId;

}