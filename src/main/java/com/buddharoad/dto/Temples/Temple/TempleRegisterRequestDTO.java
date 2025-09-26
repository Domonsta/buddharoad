// src/main/java/com/buddharoad/dto/Temples/Temple/TempleRegisterRequestDTO.java
package com.buddharoad.dto.Temples.Temple;

import com.buddharoad.domain.Region;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempleRegisterRequestDTO {
    @NotBlank(message = "사찰명은 필수 입력 항목입니다.")
    private String templeName;
    private String feature;
    @NotNull(message = "지역은 필수 입력 항목입니다.")
    private Region region;
    private String phoneNumber;
    private String homepage;
    @NotBlank(message = "주소는 필수 입력 항목입니다.")
    private String address;
    private String operatingHours;
    private String holidays;
    private String parkingInfo;
    private String admissionFee;
    private String restroomInfo;
    private String accessibility;
    private String culturalAssets;
    private String description;

    // 💡💡💡 새로 추가된 파일 목록과 그에 대한 설명 목록 💡💡💡
    private List<MultipartFile> files;
    private List<String> fileDescriptions; // 각 파일에 대한 설명 목록
}