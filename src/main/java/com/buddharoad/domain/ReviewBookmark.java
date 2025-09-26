package com.buddharoad.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter; // 💡 Lombok Getter 임포트
import lombok.Setter; // 💡 Lombok Setter 임포트

@Entity
@Getter // ✨ 모든 필드에 대한 Getter 생성
@Setter // ✨ 모든 필드에 대한 Setter 생성
@Table(name = "REVIEW_BOOKMARKS")
@IdClass(ReviewBookmarkId.class)
public class ReviewBookmark {

    @Id
    @Column(name = "MEMBER_NO", nullable = false)
    private Long memberNo;

    @Id
    @Column(name = "REVIEW_ID", nullable = false)
    private Long reviewId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberNo")
    @JoinColumn(name = "MEMBER_NO", referencedColumnName = "MEMBER_NO", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reviewId")
    @JoinColumn(name = "REVIEW_ID", referencedColumnName = "REVIEW_ID", insertable = false, updatable = false)
    private Review review;

    public ReviewBookmark() {
    }

    public ReviewBookmark(Long memberNo, Long reviewId, LocalDateTime createdAt) {
        this.memberNo = memberNo;
        this.reviewId = reviewId;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}