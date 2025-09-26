// ReviewCommentId.java
package com.buddharoad.domain;

import java.io.Serializable;
import java.util.Objects;

public class ReviewCommentId implements Serializable {
    private Long reviewId;
    private Long memberNo;

    public ReviewCommentId() {}

    public ReviewCommentId(Long reviewId, Long memberNo) {
        this.reviewId = reviewId;
        this.memberNo = memberNo;
    }

    // equals & hashCode 꼭 구현
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReviewCommentId that)) return false;
        return Objects.equals(reviewId, that.reviewId) && Objects.equals(memberNo, that.memberNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId, memberNo);
    }

    public Long getReviewId() {
        return reviewId;
    }

    public Long getMemberNo() {
        return memberNo;
    }

}
