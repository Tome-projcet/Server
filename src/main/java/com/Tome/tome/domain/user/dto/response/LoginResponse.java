package com.Tome.tome.domain.user.dto.response;

import com.Tome.tome.domain.book.entity.Book;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
        @Schema(description = "액세스 토큰 (2시간)", example = "eyJ...") String accessToken,
        @Schema(description = "온보딩 전이면 true", example = "true") boolean firstLogin
) {


    public static LoginResponse from(String accessToken, Boolean firstLogin){
        return new LoginResponse(
                accessToken, firstLogin
        );
    }
}