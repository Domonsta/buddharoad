// src/main/java/com/buddharoad/dto/Temples/Comment/TempleCommentRegisterRequestDTO.java
package com.buddharoad.dto.Temples.Comment; // 적절한 패키지 경로로 수정해주세요.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size; // Size 어노테이션 추가
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor를 포함
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempleCommentRegisterRequestDTO {

    @NotNull(message = "사찰 ID는 필수입니다.")
    private Long templeId; // 어떤 사찰에 대한 댓글인지 (URL PathVariable로 받을 수도 있음)

    // 💡💡💡 memberNo 필드 제거: 이 필드는 백엔드 SecurityContext에서 가져오므로, DTO에 포함하지 않습니다. 💡💡💡
    // private Long memberNo; // 이 필드를 제거했습니다.

    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    @Size(max = 1000, message = "댓글 내용은 1000자를 초과할 수 없습니다.") // 내용 길이 제한 추가
    private String content; // 댓글 내용
}
