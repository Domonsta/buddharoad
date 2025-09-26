package com.buddharoad.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatisticsResponseDTO {
    private Long totalMembers;
    private Long totalPosts;    // ⭐ totalTemples + totalReviews 합산 필드
    private Long totalComments; // ⭐ totalTempleComments + totalReviewComments 합산 필드

    // 추가적으로 필요하다면 세부 필드들을 여기에 포함시킬 수 있어.
    // private Long totalReviews;
    // private Long totalTemples;
    // private Long totalReviewComments;
    // private Long totalTempleComments;
}