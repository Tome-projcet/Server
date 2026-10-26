package com.Tome.tome.domain.genre.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.book.service.*;
import com.Tome.tome.domain.genre.service.GenrePreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

/** 공통 장르 목록과 로그인한 사용자의 관심 장르 관리. */
@Tag(name = "장르", description = "공통 장르 목록과 로그인한 사용자의 관심 장르 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {
    private final BookCatalogStore catalog;
    private final GenrePreferenceService preferences;
    /** 관심사 선택 화면에 사용할 공통 장르 13개의 ID와 표시 이름을 반환합니다. */
    @Operation(summary = "공통 장르 목록", description = "관심사 선택 화면에 사용할 공통 장르 13개의 ID와 표시 이름을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공통 장르 목록")
    })
    @GetMapping
    public List<GenreResponse> genres() {
        return catalog.listGenres().stream().map(g -> new GenreResponse(g.getId(), g.getName())).toList();
    }
    /** 현재 로그인한 사용자의 장르 이름과 선호 카운트를 반환합니다. */
    @Operation(summary = "내 관심 장르 조회", description = "현재 로그인한 사용자의 장르 이름과 선호 카운트를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관심 장르 목록. 선택 전에는 빈 배열")
    })
    @GetMapping("/preferences")
    public List<GenrePreferenceService.Preference> get(@Parameter(hidden = true) @AuthenticationPrincipal User user) { return preferences.get(id(user)); }
    /** 최대 5개의 관심 장르로 교체합니다. 중복 선택을 정리하고 유지된 장르의 카운트는 보존합니다. 빈 목록으로 관심사를 비울 수 있습니다. */
    @Operation(summary = "내 관심 장르 교체", description = "최대 5개의 관심 장르로 교체합니다. 중복 선택을 정리하고 유지된 장르의 카운트는 보존합니다. 빈 목록으로 관심사를 비울 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "갱신된 관심 장르 목록"),
            @ApiResponse(responseCode = "400", description = "미지원 장르, 기타 선택 또는 5개 초과", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @Parameter(name = "X-XSRF-TOKEN", in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER, description = "세션 인증 시 XSRF-TOKEN 쿠키 값을 전달. Bearer JWT 요청은 불필요")
    @PutMapping("/preferences")
    public List<GenrePreferenceService.Preference> replace(@Parameter(hidden = true) @AuthenticationPrincipal User user, @RequestBody PreferenceRequest request) {
        return preferences.replace(id(user), request.names());
    }
    /** 클라이언트가 보낸 사용자 ID 대신 인증된 주체의 ID를 사용한다. */
    private Long id(User user) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        return user.getId();
    }
    @Schema(description = "관심사 선택용 공통 장르")
    public record GenreResponse(@Schema(description = "장르 ID", example = "1") Long id,
                                @Schema(description = "표준 장르 이름", example = "소설") String name) {}
    @Schema(description = "관심 장르 교체 요청. 최대 5개, 기타 제외")
    public record PreferenceRequest(@Schema(description = "선택한 장르 이름. 빈 배열은 전체 해제", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[\"소설\",\"인문\"]") List<String> names) {}
}
