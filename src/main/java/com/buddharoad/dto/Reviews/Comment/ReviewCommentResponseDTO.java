// src/main/java/com/buddharoad/dto/Reviews/Comment/ReviewCommentResponseDTO.java
package com.buddharoad.dto.Reviews.Comment; // 적절한 패키지 경로로 수정해주세요.

import com.buddharoad.domain.ReviewComment; // ReviewComment 엔티티 import
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentResponseDTO {
    private Long reviewId; // 어떤 리뷰에 대한 댓글인지
    private Long memberNo; // 작성자 회원 번호
    private String memberUsername; // 작성자 닉네임
    private String content; // 댓글 내용
    private LocalDateTime createdAt; // 등록일
    private LocalDateTime updatedAt; // 수정일
    private Boolean isActive; // 활성 여부
    private Boolean isDeleted; // 삭제 여부 (소프트 삭제)
    private Long likeCount; // 좋아요 수 (Service에서 계산하여 설정)

    // ReviewComment 엔티티로부터 DTO를 생성하는 정적 팩토리 메서드
    // ⭐⭐ 핵심 추가: 이 댓글이 'REVIEW'에 대한 것인지 명시하는 필드 ⭐⭐
    private String boardType; // "REVIEW" 또는 "TEMPLE" (현재는 항상 "REVIEW"로 설정)

    public static ReviewCommentResponseDTO fromEntity(ReviewComment reviewComment) {
        return ReviewCommentResponseDTO.builder()
                .reviewId(reviewComment.getReview().getReviewId())
                .memberNo(reviewComment.getMember().getMemberNo())
                .memberUsername(reviewComment.getMemberUsername())
                .content(reviewComment.getContent())
                .createdAt(reviewComment.getCreatedAt())
                .updatedAt(reviewComment.getUpdatedAt())
                .isActive(reviewComment.getIsActive())
                .isDeleted(reviewComment.getIsDeleted())
                .likeCount((long) (reviewComment.getReviewCommentLikes() != null ? reviewComment.getReviewCommentLikes().size() : 0))
                .boardType("REVIEW") // ⭐⭐ ReviewComment는 항상 "REVIEW" 타입으로 설정 ⭐⭐
                .build();
    }
}