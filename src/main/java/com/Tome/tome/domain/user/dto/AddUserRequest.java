package com.Tome.tome.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 요청")
public class AddUserRequest {
    @Schema(description = "로그인 이메일", example = "test@test.com")
    private String email;
    @Schema(description = "비밀번호 (8자 이상)", example = "12345678")
    private String password;
    @Schema(description = "닉네임 (중복 불가)", example = "무관")
    private String nickname;
}
