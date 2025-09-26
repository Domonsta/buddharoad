package com.buddharoad.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode // ⭐ 중요: equals(), hashCode() 오버라이드
public class ReviewLikeId implements Serializable {

    @Column(name = "REVIEW_ID")
    private Long reviewId;

    @Column(name = "MEMBER_NO")
    private Long memberNo;

    // 생성자
    public ReviewLikeId(Long reviewId, Long memberNo) {
        this.reviewId = reviewId;
        this.memberNo = memberNo;
    }
}
