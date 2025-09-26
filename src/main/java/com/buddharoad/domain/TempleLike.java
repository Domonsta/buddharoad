package com.buddharoad.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TEMPLE_LIKES") // 💡 테이블 이름을 TEMPLE_LIKES로 변경
@IdClass(TempleLikeId.class) // 💡 복합 키 클래스도 TempleLikeId로 변경
public class TempleLike {

    @Id
    @Column(name = "MEMBER_NO", nullable = false)
    private Long memberNo;

    @Id
    @Column(name = "TEMPLE_ID", nullable = false)
    private Long templeId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberNo")
    @JoinColumn(name = "MEMBER_NO", referencedColumnName = "MEMBER_NO", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("templeId")
    @JoinColumn(name = "TEMPLE_ID", referencedColumnName = "TEMPLE_ID", insertable = false, updatable = false)
    private Temple temple;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}