package com.buddharoad.dto.Member; // DTO 패키지

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;      // 이메일 형식 검사
import jakarta.validation.constraints.NotBlank;   // null, 빈 문자열, 공백만 있는 문자열 허용 안 함
import jakarta.validation.constraints.Pattern;    // 정규표현식 패턴 검사
import jakarta.validation.constraints.Size;       // 문자열 길이 검사

@Getter
@Setter // DTO는 데이터를 받아서 설정해야 하므로 Setter 필요
public class MemberRequestDTO {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-z0-9]+$", message = "아이디는 영소문자와 숫자만 사용 가능합니다.")
    private String loginId;

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @Size(max = 100, message = "이메일은 100자 이하로 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하로 입력해주세요.")
    private String username;

    // 역할이나 계정 상태는 보통 프론트엔드에서 받지 않고 백엔드에서 기본값을 설정하거나 관리자가 지정.
    // 역할은 가입시 USER(일반회원)으로 자동 설정, 계정 상태는 관리자 설정으로만 오직 변경 가능
    // private String accountStatus; // 회원가입 DTO에는 보통 포함X
    // private Role role; // 회원가입 DTO에는 보통 포함X
}