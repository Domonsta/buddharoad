package com.buddharoad.dto.Temples.Comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempleCommentUpdateRequestDTO {

    @NotNull(message = "댓글 ID는 필수입니다.")
    private Long templeCommentId; // 수정할 댓글의 ID

    @NotNull(message = "사찰 ID는 필수입니다.")
    private Long templeId; // 어떤 사찰에 대한 댓글인지 (URL PathVariable로 받을 수도 있음)

    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    private String content; // 수정할 댓글 내용
}
