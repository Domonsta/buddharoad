package com.buddharoad.dto.Reviews.Likes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentLikeResponseDTO {
    private boolean success;
    private boolean isLiked;
    private Long likeCount;
}
