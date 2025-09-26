package com.buddharoad.domain;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable; // JPA 2.1+에서는 jakarta.persistence.Embeddable 사용

@Embeddable // 복합 키로 사용될 클래스임을 나타내
public class TempleBookmarkId implements Serializable {

    private static final long serialVersionUID = 1L; // 직렬화를 위한 고유 ID

    private Long memberNo; // @IdClass 사용할 때 엔티티 필드 이름과 일치해야 해
    private Long templeId; // @IdClass 사용할 때 엔티티 필드 이름과 일치해야 해

    // 기본 생성자는 필수!
    public TempleBookmarkId() {
    }

    // 모든 필드를 받는 생성자 (편의상)
    public TempleBookmarkId(Long memberNo, Long templeId) {
        this.memberNo = memberNo;
        this.templeId = templeId;
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

    // equals() 와 hashCode() 오버라이딩 필수!
    // JPA가 객체 동일성을 비교할 때 사용해.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TempleBookmarkId that = (TempleBookmarkId) o;
        return Objects.equals(memberNo, that.memberNo) &&
                Objects.equals(templeId, that.templeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberNo, templeId);
    }
}