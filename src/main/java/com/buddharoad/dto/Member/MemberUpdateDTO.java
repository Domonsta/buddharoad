package com.buddharoad.dto.Member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateDTO {
    // 💡 이메일과 닉네임은 변경 시에만 전송되거나, 기존값 그대로 전송될 수 있음
    // @NotBlank 대신 @Size나 @Email만 사용하여 필수가 아니게 (null 허용) 설정하거나,
    // 컨트롤러/서비스 단에서 null 체크를 통해 선택적 업데이트를 구현해야 함.
    // 여기서는 null 허용으로 설정하고, 서비스에서 null이 아닐 때만 업데이트하도록 처리할게.

    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @Size(max = 100, message = "이메일은 100자 이하로 입력해주세요.")
    private String email;

    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하로 입력해주세요.")
    private String username; // 닉네임 필드명은 `username`으로 통일 (Member 엔티티의 username과 매핑)

    // 비밀번호 변경 시 사용
    private String oldPassword; // 기존 비밀번호

    @Size(min = 8, max = 20, message = "새 비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "새 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword; // 새 비밀번호
}