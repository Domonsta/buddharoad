package com.buddharoad.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "temples")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Temple {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TEMPLE_ID_SEQ")
    @SequenceGenerator(name = "TEMPLE_ID_SEQ", sequenceName = "TEMPLE_ID_SEQ", allocationSize = 1)
    @Column(name = "TEMPLE_ID")
    private Long templeId;

    @Column(nullable = false, length = 100)
    private String templeName;

    @Column(length = 255)
    private String feature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Region region;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String homepage;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 255)
    private String operatingHours;

    @Column(length = 255)
    private String holidays;

    @Column(length = 255)
    private String parkingInfo;

    @Column(length = 100)
    private String admissionFee;

    @Column(length = 255)
    private String restroomInfo;

    @Column(length = 255)
    private String accessibility;

    @Lob
    private String description;

    @Column(length = 500)
    private String culturalAssets;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(nullable = false)
    private Long viewCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 🚨🚨🚨 TemplePhoto와의 관계 설정 (CascadeType.ALL 추가) 🚨🚨🚨
    @OneToMany(mappedBy = "temple", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Builder 사용 시 기본값으로 초기화
    private List<TemplePhoto> photos = new ArrayList<>();

    // 편의 메서드: 사진 추가
    public void addPhoto(TemplePhoto photo) {
        photos.add(photo);
        photo.setTemple(this); // 양방향 관계 설정
    }

    // 편의 메서드: 사진 제거
    public void removePhoto(TemplePhoto photo) {
        photos.remove(photo);
        photo.setTemple(null); // 양방향 관계 해제
    }

    // 💡 조회수 증가 메서드 추가
    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null) ? 1L : this.viewCount + 1;
    }
}
