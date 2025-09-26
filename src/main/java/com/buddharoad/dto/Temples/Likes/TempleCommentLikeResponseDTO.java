package com.buddharoad.dto.Temples.Likes;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class TempleCommentLikeResponseDTO {

    private Long memberNo;
    private Long templeCommentId;
    private String commentContent; // 💡 마이페이지에서 보여줄 댓글 내용
    private LocalDateTime createdAt;
    private Boolean isLiked; // 💡 '좋아요' 상태 (좋아요를 눌렀는지/안 눌렀는지)
}