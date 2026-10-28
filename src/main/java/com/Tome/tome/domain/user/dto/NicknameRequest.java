package com.Tome.tome.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NicknameRequest(
        @Schema(description = "설정할 닉네임", example = "무관") String nickname
) {
}
