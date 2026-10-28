package com.Tome.tome.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record WriterRequest(
        @Schema(description = "인생 작가 이름", example = "헤르만 헤세") String writer
) {
}
