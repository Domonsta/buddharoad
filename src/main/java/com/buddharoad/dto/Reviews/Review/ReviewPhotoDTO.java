package com.buddharoad.dto.Reviews.Review;

import com.buddharoad.domain.ReviewPhoto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewPhotoDTO {
    private Long photoId;
    private String photoUrl;
    private String description;
    private Integer photoOrder;

    public static ReviewPhotoDTO fromEntity(ReviewPhoto reviewPhoto) {
        if (reviewPhoto == null) {
            return null;
        }
        return ReviewPhotoDTO.builder()
                .photoId(reviewPhoto.getPhotoId())
                .photoUrl(reviewPhoto.getPhotoUrl())
                .description(reviewPhoto.getDescription())
                .photoOrder(reviewPhoto.getPhotoOrder())
                .build();
    }
}