package com.Tome.tome.domain.book.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.book.dto.BookResponseDto;
import com.Tome.tome.domain.book.port.in.RecommendationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

/** 사용자 선호 장르와 도서관 대출량을 반영한 추천. */
@Tag(name = "추천", description = "사용자 선호 장르와 도서관 대출량을 반영한 추천")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendationController {
    private final RecommendationUseCase recommendationUseCase;
    /** 관심 장르 선호도 75%와 로그 변환한 대출량 25%로 정렬하고 장르 다양성을 반영합니다. 관심사가 없으면 인기 도서로 보완합니다. 추천 요청은 외부 API를 호출하지 않습니다. */
    @Operation(summary = "내 취향에 맞는 도서 추천", description = "관심 장르 선호도 75%와 로그 변환한 대출량 25%로 정렬하고 장르 다양성을 반영합니다. 관심사가 없으면 인기 도서로 보완합니다. 추천 요청은 외부 API를 호출하지 않습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 이유를 포함한 목록. 후보가 없으면 빈 배열"),
            @ApiResponse(responseCode = "400", description = "추천 개수 범위 오류", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "다른 사용자의 추천 조회 시도", content = @Content)
    })
    @GetMapping("/books")
    public List<BookResponseDto> getRecommendations(@Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "기존 요청 호환용. 생략 권장, 지정하면 본인 ID만 허용") @RequestParam(required = false) Long userId, @Parameter(description = "추천 개수. 후보가 부족하면 적게 반환", schema = @Schema(defaultValue = "10", minimum = "1", maximum = "30")) @RequestParam(defaultValue = "10") int limit) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        if (userId != null && !userId.equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 추천만 조회할 수 있습니다.");
        return recommendationUseCase.getRecommendations(user.getId(), limit);
    }
}
