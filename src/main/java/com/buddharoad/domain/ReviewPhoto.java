// src/main/java/com/buddharoad/domain/ReviewPhoto.java
package com.buddharoad.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter // 양방향 관계 설정을 위해 필요 (그리고 description 필드 업데이트를 위해 @Setter 필요)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "REVIEW_PHOTOS")
@SequenceGenerator(name = "REVIEW_PHOTO_SEQ", sequenceName = "REVIEW_PHOTO_SEQ", allocationSize = 1)
public class ReviewPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REVIEW_PHOTO_SEQ")
    @Column(name = "PHOTO_ID")
    private Long photoId;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "PHOTO_URL", nullable = false, length = 1000)
    private String photoUrl;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "PHOTO_ORDER")
    private Integer photoOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID", nullable = false)
    private Review review;

    // --- 비즈니스 로직 메서드 ---

    // Review 엔티티와 양방향 관계 설정을 위한 편의 메서드
    public void setReview(Review review) {
        if (this.review != null) {
            this.review.getPhotos().remove(this);
        }
        this.review = review;
        if (review != null) {
            review.getPhotos().add(this);
        }
    }

    // ⭐⭐ 설명 업데이트 메서드 (이전에 있었던 것) ⭐⭐
    public void updateDescription(String newDescription) {
        this.description = newDescription;
    }

    // ✨✨✨ 새로 추가할 수 있는 메서드 (updateDescriptionAndOrder 오류 시) ✨✨✨
    public void updateDescriptionAndOrder(String newDescription, Integer newOrder) {
        this.description = newDescription;
        this.photoOrder = newOrder;
    }
}