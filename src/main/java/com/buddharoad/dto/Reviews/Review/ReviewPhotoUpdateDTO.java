package com.buddharoad.dto.Reviews.Review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewPhotoUpdateDTO {
    private Long photoId; // 기존 사진 수정/삭제 시 필요
    private String photoUrl;
    private String description;
    private Integer photoOrder; // 사진 순서 변경 시 필요
}