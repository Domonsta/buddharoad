package com.buddharoad.dto.Temples.Likes;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TempleLikeRequestDTO {

    @NotNull(message = "사찰 ID는 필수입니다.")
    private Long templeId;

}