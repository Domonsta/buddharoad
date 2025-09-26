package com.buddharoad.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Table(name = "REVIEW_LIKES")
@IdClass(ReviewLikeId.class) // ⭐ 복합키 클래스 지정
public class ReviewLike {

    @Id
    @Column(name = "REVIEW_ID")
    private Long reviewId;

    @Id
    @Column(name = "MEMBER_NO")
    private Long memberNo;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ⭐ 편의 메서드를 통해 Review와 Member 엔티티와 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reviewId") // 복합키의 reviewId 필드와 매핑
    @JoinColumn(name = "REVIEW_ID")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberNo") // 복합키의 memberNo 필드와 매핑
    @JoinColumn(name = "MEMBER_NO")
    private Member member;
}