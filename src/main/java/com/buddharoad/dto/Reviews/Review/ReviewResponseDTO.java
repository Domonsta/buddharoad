package com.buddharoad.dto.Reviews.Review;

import com.buddharoad.domain.Review;
import com.buddharoad.domain.ReviewPhoto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private Long reviewId;
    private Long templeId;
    private String templeName; // 사찰명 필드
    private String region; // ✨ 사찰 지역 필드 추가 ✨
    private Long memberNo;
    private String memberUsername; // 작성자명 필드
    private String title;
    private String content;
    private Integer rating;
    private Long viewCount;
    private String tags;
    private LocalDate visitedAt; // Review 엔티티와 동일하게 LocalDate 유지
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private Boolean isDeleted;
    private List<ReviewPhotoDTO> photos;
    private String photosUrls; // 필요시 모든 사진 URL을 한 문자열로 합쳐서 보여줄 수 있음
    private Long likeCount;

    public static ReviewResponseDTO fromEntity(Review review) {
        if (review == null) {
            return null;
        }

        List<ReviewPhotoDTO> photoDTOs = review.getPhotos() != null ?
                review.getPhotos().stream()
                        .map(ReviewPhotoDTO::fromEntity)
                        .collect(Collectors.toList()) :
                List.of();

        return ReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                // 🚨🚨🚨 이 부분이 중요! Review 엔티티의 getTemple()을 통해 Temple ID 가져오기 🚨🚨🚨
                .templeId(review.getTemple() != null ? review.getTemple().getTempleId() : null)
                .templeName(review.getTemple() != null ? review.getTemple().getTempleName() : "정보 없음")
                .region(review.getTemple() != null && review.getTemple().getRegion() != null ? review.getTemple().getRegion().name() : "정보 없음") // .name()을 사용하여 ENUM을 String으로 변환
                // 🚨🚨🚨 이 부분도 중요! Review 엔티티의 getMember()를 통해 Member No 가져오기 🚨🚨🚨
                .memberNo(review.getMember() != null ? review.getMember().getMemberNo() : null)
                // memberUsername은 Review 엔티티에 직접 필드로 있으니 그대로 사용
                .memberUsername(review.getMemberUsername())
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .viewCount(review.getViewCount())
                .tags(review.getTags())
                .visitedAt(review.getVisitedAt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .isActive(review.getIsActive())
                .isDeleted(review.getIsDeleted())
                .photos(photoDTOs)
                .photosUrls(photoDTOs.stream()
                        .map(ReviewPhotoDTO::getPhotoUrl)
                        .collect(Collectors.joining(", ")))
                .likeCount(review.getLikeCount())
                .build();
    }
}