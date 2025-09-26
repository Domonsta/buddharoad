// src/main/java/com/buddharoad/dto/Reviews/Review/ReviewUpdateRequestDTO.java
package com.buddharoad.dto.Reviews.Review;

import com.buddharoad.domain.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime; // LocalDateTime으로 통일
import java.util.List; // ⭐ List import 추가!

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewUpdateRequestDTO {

    @NotNull(message = "회원 번호는 필수 입력 항목입니다.")
    private Long memberNo;

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String content;

    @NotNull(message = "별점은 필수 입력 항목입니다.")
    @Min(value = 1, message = "별점은 최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 최대 5점 이하여야 합니다.")
    private Integer rating;

    private String tags;

    private LocalDate visitedAt;

    // ⭐⭐⭐ 중요: 이 두 필드를 추가해야 해! ⭐⭐⭐
    private List<ReviewPhotoUpdateDTO> updatedPhotoDescriptions; // 기존 사진의 photoId와 description
    private List<String> newPhotoDescriptions; // 새로운 사진의 description (순서는 newFiles와 매칭)


    // DTO를 엔티티로 변환하는 메서드 (업데이트 시 사용)
    public Review toEntity() {
        return Review.builder()
                .title(this.title)
                .content(this.content)
                .rating(this.rating)
                .tags(this.tags)
                .visitedAt(this.visitedAt)
                .build();
    }
}