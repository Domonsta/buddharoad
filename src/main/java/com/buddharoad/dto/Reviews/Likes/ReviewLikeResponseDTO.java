package com.buddharoad.dto.Reviews.Likes;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class ReviewLikeResponseDTO {

    // 요청 성공 여부
    private boolean success;

    // 현재 좋아요 상태 (true: 좋아요 상태, false: 좋아요 취소 상태)
    private boolean isLiked;

    // 현재 좋아요 개수
    private Long likeCount;
}