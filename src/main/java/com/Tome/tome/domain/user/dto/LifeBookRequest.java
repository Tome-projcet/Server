package com.Tome.tome.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 카카오 검색 또는 ISBN 상세 조회에서 선택한 책의 ISBN만 전달한다. */
@Schema(description = "인생 책 선택 요청. 도서 정보는 서버가 조회한다")
public record LifeBookRequest(
        @Schema(description = "유효한 ISBN10 또는 ISBN13", example = "9788983921987", requiredMode = Schema.RequiredMode.REQUIRED)
        String isbn) {}
