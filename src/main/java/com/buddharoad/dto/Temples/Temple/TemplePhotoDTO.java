package com.buddharoad.dto.Temples.Temple;

import com.buddharoad.domain.TemplePhoto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplePhotoDTO {
    private Long photoId;
    private String photoUrl;
    private String description;
    private Integer photoOrder;

    public static TemplePhotoDTO fromEntity(TemplePhoto templePhoto) {
        if (templePhoto == null) {
            return null;
        }
        return TemplePhotoDTO.builder()
                .photoId(templePhoto.getPhotoId())
                .photoUrl(templePhoto.getPhotoUrl())
                .description(templePhoto.getDescription())
                .photoOrder(templePhoto.getPhotoOrder())
                .build();
    }
}