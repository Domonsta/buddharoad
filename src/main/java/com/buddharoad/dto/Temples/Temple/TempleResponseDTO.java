package com.buddharoad.dto.Temples.Temple;

import com.buddharoad.domain.Region;
import com.buddharoad.domain.Temple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempleResponseDTO {
    private Long templeId;
    private String templeName;
    private String feature;
    private Region region;
    private String phoneNumber;
    private String homepage;
    private String address;
    private String operatingHours;
    private String holidays;
    private String parkingInfo;
    private String admissionFee;
    private String restroomInfo;
    private String accessibility;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long viewCount;
    private String culturalAssets;
    private Boolean isActive;
    private Boolean isDeleted;
    private List<TemplePhotoDTO> photos;

    // Temple 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static TempleResponseDTO fromEntity(Temple temple) {
        if (temple == null) {
            return null;
        }
        return TempleResponseDTO.builder()
                .templeId(temple.getTempleId())
                .templeName(temple.getTempleName())
                .feature(temple.getFeature())
                .region(temple.getRegion())
                .phoneNumber(temple.getPhoneNumber())
                .homepage(temple.getHomepage())
                .address(temple.getAddress())
                .operatingHours(temple.getOperatingHours())
                .holidays(temple.getHolidays())
                .parkingInfo(temple.getParkingInfo())
                .admissionFee(temple.getAdmissionFee())
                .restroomInfo(temple.getRestroomInfo())
                .accessibility(temple.getAccessibility())
                .description(temple.getDescription())
                .createdAt(temple.getCreatedAt())
                .updatedAt(temple.getUpdatedAt())
                .viewCount(temple.getViewCount())
                .culturalAssets(temple.getCulturalAssets())
                .isActive(temple.getIsActive())
                .isDeleted(temple.getIsDeleted())
                .photos(temple.getPhotos() != null ?
                        temple.getPhotos().stream()
                                .map(TemplePhotoDTO::fromEntity)
                                .collect(Collectors.toList()) :
                        List.of()) // 사진이 없을 경우 빈 리스트 반환
                .build();
    }
}