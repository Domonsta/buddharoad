// src/main/java/com/buddharoad/dto/Admin/AdminUpdateDTO.java (새로 생성)
package com.buddharoad.dto.Admin; // Admin 관련 DTO 패키지

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // 데이터 바인딩을 위해 Setter 필요

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateDTO {
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @Size(max = 100, message = "이메일은 100자 이하로 입력해주세요.")
    private String email;

    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하로 입력해주세요.")
    private String username; // 닉네임

    private String oldPassword; // 기존 비밀번호

    @Size(min = 8, max = 20, message = "새 비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "새 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword; // 새 비밀번호
}