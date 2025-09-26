package com.buddharoad.dto.Member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberWithdrawalDTO {
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password; // 회원 탈퇴 시 본인 확인을 위한 비밀번호
}