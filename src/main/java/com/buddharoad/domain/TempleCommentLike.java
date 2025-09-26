package com.buddharoad.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "TEMPLE_COMMENT_LIKES") // 💡 테이블 이름 설정
@IdClass(TempleCommentLikeId.class) // 💡 복합 키 클래스 지정
public class TempleCommentLike {

    @Id
    @Column(name = "MEMBER_NO", nullable = false)
    private Long memberNo;

    @Id
    @Column(name = "TEMPLE_COMMENT_ID", nullable = false)
    private Long templeCommentId;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberNo")
    @JoinColumn(name = "MEMBER_NO", referencedColumnName = "MEMBER_NO", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("templeCommentId")
    @JoinColumn(name = "TEMPLE_COMMENT_ID", referencedColumnName = "TEMPLE_COMMENT_ID", insertable = false, updatable = false)
    @Setter(AccessLevel.PROTECTED) // 양방향 관계 설정을 위해 setter 추가
    private TempleComment templeComment; // 💡 TempleComment 엔티티와 연결

}