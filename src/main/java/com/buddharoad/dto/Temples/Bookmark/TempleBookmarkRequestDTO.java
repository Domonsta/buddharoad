package com.buddharoad.dto.Temples.Bookmark;

import jakarta.validation.constraints.NotNull; // 유효성 검사 (선택 사항이지만 추천!)
import lombok.Data;

@Data
public class TempleBookmarkRequestDTO {

    @NotNull(message = "회원 번호는 필수입니다.") // null 값 허용 안 함
    private Long memberNo;

    @NotNull(message = "사찰 ID는 필수입니다.") // null 값 허용 안 함
    private Long templeId;

}
