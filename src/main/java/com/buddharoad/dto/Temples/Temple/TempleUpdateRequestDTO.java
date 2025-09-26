package com.buddharoad.dto.Temples.Temple;

import com.buddharoad.domain.Region;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempleUpdateRequestDTO {
    // 사찰 기본 정보 (수정과 동일)
    @NotBlank(message = "사찰 이름은 필수입니다.")
    @Size(max = 100, message = "사찰 이름은 100자를 초과할 수 없습니다.")
    private String templeName;

    @Size(max = 255, message = "특징은 255자를 초과할 수 없습니다.")
    private String feature;

    @NotNull(message = "지역은 필수입니다.")
    private Region region;

    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다.")
    private String phoneNumber;

    @Size(max = 255, message = "홈페이지 URL은 255자를 초과할 수 없습니다.")
    private String homepage;

    @NotBlank(message = "사찰 주소는 필수입니다.")
    @Size(max = 255, message = "사찰 주소는 255자를 초과할 수 없습니다.")
    private String address;

    @Size(max = 255, message = "운영 시간은 255자를 초과할 수 없습니다.")
    private String operatingHours;

    @Size(max = 255, message = "휴일 정보는 255자를 초과할 수 없습니다.")
    private String holidays;

    @Size(max = 255, message = "주차 정보는 255자를 초과할 수 없습니다.")
    private String parkingInfo;

    @Size(max = 100, message = "입장료는 100자를 초과할 수 없습니다.")
    private String admissionFee;

    @Size(max = 255, message = "화장실 정보는 255자를 초과할 수 없습니다.")
    private String restroomInfo;

    @Size(max = 255, message = "접근성은 255자를 초과할 수 없습니다.")
    private String accessibility;

    @Size(max = 20000, message = "설명은 20000자를 초과할 수 없습니다.")
    private String description;

    @Size(max = 500, message = "문화재 정보는 500자를 초과할 수 없습니다.")
    private String culturalAssets;

    // 새로 추가: 사찰 활성화 상태 필드
    private Boolean isActive;

    // 새로 추가할 파일 리스트
    private List<MultipartFile> newPhotos;

    // 삭제할 사진 ID 리스트
    private List<Long> deletedPhotoIds;

    // 수정할 사진 정보 리스트
    private List<TemplePhotoUpdateDTO> updatedPhotoInfo;

    // 💡 새로 추가된 사진 설명 목록 (프론트엔드에서 newFileDescriptions로 보냄)
    private List<String> newFileDescriptions;
}
