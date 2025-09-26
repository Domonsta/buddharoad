package com.buddharoad.domain;

import com.buddharoad.dto.Temples.Temple.TemplePhotoDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter // 양방향 관계 설정을 위해 필요 (또는 addTemplePhoto() 같은 편의 메서드 사용)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "TEMPLE_PHOTOS") // DB 테이블명은 "TEMPLE_PHOTOS"로 가정했어!
public class TemplePhoto {

    @Id
    // 💡💡💡 여기 바뀌는 부분이야! IDENTITY -> SEQUENCE 💡💡💡
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TEMPLE_PHOTO_SEQ")
    @SequenceGenerator(name = "TEMPLE_PHOTO_SEQ", sequenceName = "TEMPLE_PHOTO_SEQ", allocationSize = 1)
    @Column(name = "PHOTO_ID")
    private Long photoId;

    @Column(name = "PHOTO_URL", nullable = false, length = 1000) // 사진 URL 저장 (충분한 길이로)
    private String photoUrl;

    @Column(name = "DESCRIPTION", length = 500) // 사진에 대한 설명 (선택 사항)
    private String description;

    @Column(name = "PHOTO_ORDER") // 사진이 표시될 순서 (선택 사항)
    private Integer photoOrder;

    // Temple 엔티티와의 N:1 관계 설정
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 (TemplePhoto를 조회할 때 Temple을 바로 로딩하지 않음)
    @JoinColumn(name = "TEMPLE_ID", nullable = false) // 외래키 컬럼명, TEMPLE 테이블의 ID를 참조
    private Temple temple; // 연관된 Temple 엔티티

    @CreationTimestamp // 엔티티 생성 시 자동으로 현재 시각 저장
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티 수정 시 자동으로 현재 시각 저장
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // --- 비즈니스 로직 메서드 (선택 사항) ---

    // 편의 메서드: 이 사진이 어떤 Temple에 속하는지 설정 (양방향 관계 유지를 위해 사용)
    public void setTemple(Temple temple) {
        if (this.temple != null) { // 기존 Temple과의 관계를 끊을 때
            this.temple.getPhotos().remove(this);
        }
        this.temple = temple;
        if (temple != null) { // 새로운 Temple과의 관계를 맺을 때
            temple.getPhotos().add(this);
        }
    }

    // 사진 URL 변경 메서드 (필요시)
    public void updatePhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    // 설명 변경 메서드 (필요시)
    public void updateDescription(String description) {
        this.description = description;
    }

    // 순서 변경 메서드 (필요시)
    public void updatePhotoOrder(Integer photoOrder) {
        this.photoOrder = photoOrder;
    }

    public void updatePhotoInfo(String description, Integer photoOrder) {
        if (description != null) this.description = description;
        if (photoOrder != null) this.photoOrder = photoOrder;
    }

    // 🌟🌟🌟 이 메서드가 없어서 오류가 났던 거야! 꼭 추가해줘! 🌟🌟🌟
    public static TemplePhotoDTO fromEntity(TemplePhoto templePhoto) {
        if (templePhoto == null) {
            return null;
        }
        return TemplePhotoDTO.builder()
                .photoId(templePhoto.getPhotoId())
                .photoUrl(templePhoto.getPhotoUrl())
                .description(templePhoto.getDescription())
                .photoOrder(templePhoto.getPhotoOrder())
                .build();
    }
}