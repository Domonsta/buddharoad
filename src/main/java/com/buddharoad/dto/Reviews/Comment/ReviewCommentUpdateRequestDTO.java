// src/main/java/com/buddharoad/dto/Reviews/Comment/ReviewCommentUpdateRequestDTO.java
package com.buddharoad.dto.Reviews.Comment; // 적절한 패키지 경로로 수정해주세요.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size; // Size 어노테이션 추가
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentUpdateRequestDTO {

    @NotNull(message = "리뷰 ID는 필수입니다.")
    private Long reviewId; // 어떤 리뷰에 대한 댓글인지

    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    @Size(max = 1000, message = "댓글 내용은 1000자를 초과할 수 없습니다.") // 내용 길이 제한 추가
    private String content; // 수정할 댓글 내용
}