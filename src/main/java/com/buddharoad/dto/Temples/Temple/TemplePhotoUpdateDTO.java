package com.buddharoad.dto.Temples.Temple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplePhotoUpdateDTO {
    @NotNull(message = "사진 ID는 필수입니다.")
    private Long photoId; // 수정할 사진의 ID

    @Size(max = 500, message = "사진 설명은 500자를 초과할 수 없습니다.")
    private String description;

    private Integer photoOrder; // 사진 순서
}