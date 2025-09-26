// src/main/java/com/buddharoad/domain/ReviewComment.java
package com.buddharoad.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "REVIEW_COMMENTS")
public class ReviewComment implements Serializable {

    @EmbeddedId
    private ReviewCommentId id;

    @MapsId("memberNo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO", nullable = false)
    private Member member;

    @MapsId("reviewId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID", nullable = false)
    private Review review;

    @Column(name = "MEMBER_USERNAME", nullable = false, length = 50)
    private String memberUsername;

    @Column(name = "CONTENT", columnDefinition = "CLOB", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "IS_ACTIVE", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "IS_DELETED", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "LIKE_COUNT", nullable = true)
    @Builder.Default
    private Long likeCount = 0L;

    @OneToMany(mappedBy = "reviewComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewCommentLike> reviewCommentLikes = new ArrayList<>();

    // --- 비즈니스 로직 메서드 ---
    public void updateContent(String newContent) {
        this.content = newContent;
    }
    public void markAsDeleted() {
        this.isDeleted = true;
        this.isActive = false;
    }
    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null) ? 1L : this.likeCount + 1;
    }
    public void decrementLikeCount() {
        if (this.likeCount != null && this.likeCount > 0) {
            this.likeCount--;
        }
    }
}