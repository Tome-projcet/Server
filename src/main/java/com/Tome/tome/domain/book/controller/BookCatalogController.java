package com.Tome.tome.domain.book.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.Tome.tome.domain.book.infra.ExternalBook;
import com.Tome.tome.global.config.BookApiConfig;
import com.Tome.tome.domain.book.service.BookCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.List;

/** 카카오 검색·상세 정보와 정보나루 인기 대출 도서 수집. */
@Tag(name = "도서", description = "카카오 검색·상세 정보와 정보나루 인기 대출 도서 수집")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookCatalogController {
    private final BookCatalogService catalog;
    private final BookApiConfig config;
    /** 카카오에서 검색하며 실패 시 저장된 제목·저자 검색 결과로 대체합니다. 검색 결과는 DB에 자동 저장하지 않습니다. */
    @Operation(summary = "도서 검색", description = "카카오에서 검색하며 실패 시 저장된 제목·저자 검색 결과로 대체합니다. 검색 결과는 DB에 자동 저장하지 않습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 목록. 검색 결과가 없으면 빈 배열"),
            @ApiResponse(responseCode = "400", description = "검색어 또는 페이지·개수 범위 오류", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "외부 API 사용 불가이며 DB 대체 결과도 없음", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @GetMapping("/search")
    public List<ExternalBook> search(@Parameter(description = "도서 검색어. 1~200자", example = "소설", required = true) @RequestParam String query, @Parameter(description = "결과 페이지. 1~50", schema = @Schema(defaultValue = "1", minimum = "1", maximum = "50")) @RequestParam(defaultValue = "1") int page,
                                    @Parameter(description = "페이지당 도서 수. 1~50", schema = @Schema(defaultValue = "10", minimum = "1", maximum = "50")) @RequestParam(defaultValue = "10") int size) {
        if (query.isBlank() || query.length() > 200 || page < 1 || page > 50 || size < 1 || size > 50)
            throw new IllegalArgumentException("검색어는 1~200자, 페이지와 개수는 1~50입니다.");
        return catalog.search(query.trim(), page, size);
    }
    /** ISBN10/13을 검사하고 카카오의 상세 정보와 정보나루의 분류를 통합합니다. 외부 결과가 없으면 저장된 책을 조회합니다. */
    @Operation(summary = "ISBN으로 도서 상세 조회", description = "ISBN10/13을 검사하고 카카오의 상세 정보와 정보나루의 분류를 통합합니다. 외부 결과가 없으면 저장된 책을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통합 도서 상세 정보"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 ISBN", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "외부 API와 DB에 해당 도서가 없음", content = @Content),
            @ApiResponse(responseCode = "503", description = "키 미설정 또는 API 연결 실패로 조회 불가", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @GetMapping("/{isbn}")
    public ExternalBook details(@Parameter(description = "검사 숫자가 유효한 ISBN10 또는 ISBN13", example = "9788983921987", required = true) @PathVariable String isbn) { return catalog.details(isbn); }
    /** 정보나루 목록을 ISBN 기준으로 저장·갱신하고 카카오로 최대 20권을 보완합니다. 로그인과 동기화 토큰이 모두 필요합니다. 카카오 실패 시 정보나루 데이터로 저장하고 경고를 반환합니다. */
    @Operation(summary = "인기 대출 도서 수집·갱신", description = "정보나루 목록을 ISBN 기준으로 저장·갱신하고 카카오로 최대 20권을 보완합니다. 로그인과 동기화 토큰이 모두 필요합니다. 카카오 실패 시 정보나루 데이터로 저장하고 경고를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장·갱신 건수, 기간 및 경고 목록"),
            @ApiResponse(responseCode = "400", description = "날짜 또는 수집 개수 범위 오류", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "동기화 토큰 누락 또는 불일치", content = @Content),
            @ApiResponse(responseCode = "503", description = "동기화 토큰 미설정 또는 정보나루 조회 실패", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @Parameter(name = "X-XSRF-TOKEN", in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER, description = "세션 인증 시 XSRF-TOKEN 쿠키 값을 전달. Bearer JWT 요청은 불필요")
    @PostMapping("/sync/popular")
    public BookCatalogService.SyncResult sync(@Parameter(description = "서버 BOOK_SYNC_TOKEN과 일치하는 운영용 비밀값", required = true, schema = @Schema(type = "string", format = "password")) @RequestHeader(value = "X-Book-Sync-Token", required = false) String token,
            @Parameter(description = "대출 집계 시작일. 기본값: 종료일 29일 전", example = "2026-09-26") @RequestParam(required = false) LocalDate start, @Parameter(description = "대출 집계 종료일. 기본값: 한국 시간 어제. 미래 날짜 금지, 시작일과 차이 최대 366일", example = "2026-10-25") @RequestParam(required = false) LocalDate end,
            @Parameter(description = "수집 개수. 1~100", schema = @Schema(defaultValue = "50", minimum = "1", maximum = "100")) @RequestParam(defaultValue = "50") int size) {
        if (config.getSyncToken().isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "도서 동기화가 설정되지 않았습니다.");
        if (token == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), config.getSyncToken().getBytes(StandardCharsets.UTF_8)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "도서 동기화 권한이 없습니다.");
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate until = end == null ? today.minusDays(1) : end;
        LocalDate from = start == null ? until.minusDays(29) : start;
        if (size < 1 || size > 100 || from.isAfter(until) || until.isAfter(today)
                || java.time.temporal.ChronoUnit.DAYS.between(from, until) > 366)
            throw new IllegalArgumentException("조회 기간은 최대 1년, 수집 개수는 1~100권입니다.");
        return catalog.syncPopular(from, until, size);
    }
}
