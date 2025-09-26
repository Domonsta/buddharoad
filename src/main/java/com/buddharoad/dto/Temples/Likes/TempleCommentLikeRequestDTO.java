package com.buddharoad.dto.Temples.Likes;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TempleCommentLikeRequestDTO {

    @NotNull(message = "댓글 ID는 필수입니다.")
    private Long templeCommentId;

}