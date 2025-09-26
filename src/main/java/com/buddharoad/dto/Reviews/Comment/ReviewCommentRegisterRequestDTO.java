// src/main/java/com/buddharoad/dto/Reviews/Comment/ReviewCommentRegisterRequestDTO.java
package com.buddharoad.dto.Reviews.Comment; // 적절한 패키지 경로로 수정해주세요.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentRegisterRequestDTO {

    @NotNull(message = "리뷰 ID는 필수입니다.")
    private Long reviewId; // 어떤 리뷰에 대한 댓글인지

    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    @Size(max = 1000, message = "댓글 내용은 1000자를 초과할 수 없습니다.")
    private String content; // 댓글 내용
}