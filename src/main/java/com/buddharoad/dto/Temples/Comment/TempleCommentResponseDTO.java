// src/main/java/com/buddharoad/dto/Temples/Comment/TempleCommentResponseDTO.java
package com.buddharoad.dto.Temples.Comment;

import com.buddharoad.domain.TempleComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempleCommentResponseDTO {
    private Long templeCommentId;
    private Long templeId; // 어떤 사찰에 대한 댓글인지
    private String templeName; // ⭐ 추가: 원본 사찰 이름
    private Long memberNo; // 작성자 회원 번호
    private String memberUsername; // 💡💡💡 작성자 닉네임(username) 필드 추가 💡💡💡
    private String content; // 댓글 내용
    private LocalDateTime createdAt; // 등록일
    private LocalDateTime updatedAt; // 수정일
    private Integer viewCount; // 조회수
    private Boolean isActive; // 활성 여부
    private Boolean isDeleted; // 삭제 여부 (소프트 삭제)
    private Long likeCount; // 좋아요 수 (Service에서 계산하여 설정)

    // ⭐⭐ 핵심 추가: 이 댓글이 'TEMPLE'에 대한 것인지 명시하는 필드 ⭐⭐
    private String boardType; // "REVIEW" 또는 "TEMPLE" (현재는 항상 "TEMPLE"로 설정)

    // TempleComment 엔티티로부터 DTO를 생성하는 정적 팩토리 메서드
    public static TempleCommentResponseDTO fromEntity(TempleComment templeComment) {
        return TempleCommentResponseDTO.builder()
                .templeCommentId(templeComment.getTempleCommentId())
                .templeId(templeComment.getTemple() != null ? templeComment.getTemple().getTempleId() : null)
                .templeName(templeComment.getTemple() != null ? templeComment.getTemple().getTempleName() : "정보 없음") // ⭐ 사찰 이름 설정
                .memberNo(templeComment.getMember() != null ? templeComment.getMember().getMemberNo() : null)
                .memberUsername(templeComment.getMemberUsername())
                .content(templeComment.getContent())
                .createdAt(templeComment.getCreatedAt())
                .updatedAt(templeComment.getUpdatedAt())
                .viewCount(templeComment.getViewCount())
                .isActive(templeComment.getIsActive())
                .isDeleted(templeComment.getIsDeleted())
                // TempleCommentLike 엔티티가 구현되었다면, size()를 통해 좋아요 수를 가져올 수 있어.
                // 여기서는 TempleComment 엔티티에 templeCommentLikes 리스트가 있으므로,
                // 리스트의 size를 가져오면 돼. 만약 null일 경우 0으로 처리.
                .likeCount((long) (templeComment.getTempleCommentLikes() != null ? templeComment.getTempleCommentLikes().size() : 0))
                .boardType("TEMPLE") // ⭐⭐ TempleComment는 항상 "TEMPLE" 타입으로 설정 ⭐⭐
                .build();
    }
}