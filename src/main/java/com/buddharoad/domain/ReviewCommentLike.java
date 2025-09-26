// src/main/java/com/buddharoad/domain/ReviewCommentLike.java
package com.buddharoad.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Entity
@Table(name = "REVIEW_COMMENT_LIKES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentLike implements Serializable {

    @EmbeddedId
    private ReviewCommentLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "REVIEW_ID", referencedColumnName = "REVIEW_ID", insertable = false, updatable = false),
            @JoinColumn(name = "COMMENT_MEMBER_NO", referencedColumnName = "MEMBER_NO", insertable = false, updatable = false)
    })
    private ReviewComment reviewComment;
}
