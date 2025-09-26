package com.buddharoad.domain; // 💡 패키지 경로는 너의 프로젝트에 맞게 조정해줘

import jakarta.persistence.*; // JPA 관련 어노테이션 임포트
import java.time.LocalDateTime; // 날짜와 시간을 다루기 위한 임포트

@Entity // 이 클래스가 JPA 엔티티임을 나타내
@Table(name = "TEMPLE_BOOKMARKS") // 매핑될 테이블 이름
@IdClass(TempleBookmarkId.class) // 복합 기본 키 클래스를 지정
public class TempleBookmark {

    @Id // 복합 키의 첫 번째 부분
    @Column(name = "MEMBER_NO", nullable = false)
    private Long memberNo;

    @Id // 복합 키의 두 번째 부분
    @Column(name = "TEMPLE_ID", nullable = false)
    private Long templeId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt; // 찜한 시간

    // 연관 관계 매핑 (선택 사항이지만, 보통 이렇게 매핑해)
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩으로 성능 최적화
    @MapsId("memberNo") // TempleBookmarkId의 memberNo 필드와 매핑
    @JoinColumn(name = "MEMBER_NO", referencedColumnName = "MEMBER_NO", insertable = false, updatable = false)
    private Member member; // Member 엔티티가 있다고 가정

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩으로 성능 최적화
    @MapsId("templeId") // TempleBookmarkId의 templeId 필드와 매핑
    @JoinColumn(name = "TEMPLE_ID", referencedColumnName = "TEMPLE_ID", insertable = false, updatable = false)
    private Temple temple; // Temple 엔티티가 있다고 가정

    // 기본 생성자
    public TempleBookmark() {
    }

    // 모든 필드를 포함하는 생성자 (편의상)
    public TempleBookmark(Long memberNo, Long templeId, LocalDateTime createdAt) {
        this.memberNo = memberNo;
        this.templeId = templeId;
        this.createdAt = createdAt;
    }

    // Getter와 Setter
    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public Long getTempleId() {
        return templeId;
    }

    public void setTempleId(Long templeId) {
        this.templeId = templeId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Temple getTemple() {
        return temple;
    }

    public void setTemple(Temple temple) {
        this.temple = temple;
    }

    @PrePersist // 엔티티가 영속화되기 전에 호출되는 메서드
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(); // 현재 시간으로 설정
        }
    }
}