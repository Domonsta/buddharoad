package com.buddharoad.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ReviewCommentLikeId implements Serializable {

    @Column(name = "MEMBER_NO")  // REVIEW_COMMENT_LIKES 테이블에서 '회원 번호' (좋아요 누른 사람)
    private Long memberNo;

    @Column(name = "REVIEW_ID")  // 리뷰 ID
    private Long reviewId;

    @Column(name = "COMMENT_MEMBER_NO") // 이건 review_comments의 member_no를 참조하는 FK로 쓰임
    private Long commentMemberNo;

    public ReviewCommentLikeId() {}

    public ReviewCommentLikeId(Long memberNo, Long reviewId, Long commentMemberNo) {
        this.memberNo = memberNo;
        this.reviewId = reviewId;
        this.commentMemberNo = commentMemberNo;
    }

    // equals, hashCode, getter, setter 생략
}
