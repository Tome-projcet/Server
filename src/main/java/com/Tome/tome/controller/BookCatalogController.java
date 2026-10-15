package com.Tome.tome.controller;

import com.Tome.tome.catalog.ExternalBook;
import com.Tome.tome.config.BookApiConfig;
import com.Tome.tome.service.BookCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookCatalogController {
    private final BookCatalogService catalog;
    private final BookApiConfig config;
    @GetMapping("/search")
    public List<ExternalBook> search(@RequestParam String query, @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        if (query.isBlank() || query.length() > 200 || page < 1 || page > 50 || size < 1 || size > 50)
            throw new IllegalArgumentException("검색어는 1~200자, 페이지와 개수는 1~50입니다.");
        return catalog.search(query.trim(), page, size);
    }
    @GetMapping("/{isbn}")
    public ExternalBook details(@PathVariable String isbn) { return catalog.details(isbn); }
    @PostMapping("/sync/popular")
    public BookCatalogService.SyncResult sync(@RequestHeader(value = "X-Book-Sync-Token", required = false) String token,
            @RequestParam(required = false) LocalDate start, @RequestParam(required = false) LocalDate end,
            @RequestParam(defaultValue = "50") int size) {
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
