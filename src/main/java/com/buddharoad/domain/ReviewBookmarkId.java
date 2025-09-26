package com.buddharoad.domain;

import java.io.Serializable;
import java.util.Objects;

public class ReviewBookmarkId implements Serializable {

    private Long memberNo; // 💡 찜한 회원 번호
    private Long reviewId; // 💡 찜한 리뷰 ID

    public ReviewBookmarkId() {
    }

    // `equals()`와 `hashCode()`를 재정의해서 객체를 유일하게 식별해줘.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewBookmarkId that = (ReviewBookmarkId) o;
        return Objects.equals(memberNo, that.memberNo) &&
                Objects.equals(reviewId, that.reviewId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberNo, reviewId);
    }
}