package com.buddharoad.dto.Reviews.Likes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLikeRequestDTO {

    // 좋아요를 누를 리뷰의 ID
    private Long reviewId;
}