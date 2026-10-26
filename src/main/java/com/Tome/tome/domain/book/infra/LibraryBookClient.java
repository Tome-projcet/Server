package com.Tome.tome.domain.book.infra;

import com.Tome.tome.global.config.BookApiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.*;
import static com.Tome.tome.domain.book.infra.BookHttpClient.*;

/** 정보나루 인기 대출·상세 응답을 읽고 ISBN, KDC 분류, 대출량을 추출한다. */
@Component
@RequiredArgsConstructor
public class LibraryBookClient {
    private final BookApiConfig config;
    private final BookHttpClient http;
    private final GenreTaxonomy taxonomy;
    /** 기간별 인기 대출 도서의 response/docs/doc 구조를 순회하며 ISBN 중복을 제거한다. */
    public List<ExternalBook> popular(LocalDate start, LocalDate end, int size) {
        var uri = builder("loanItemSrch").queryParam("startDt", start).queryParam("endDt", end)
                .queryParam("pageNo", 1).queryParam("pageSize", size).build().encode().toUri();
        JsonNode response = response(http.get(uri, null, "정보나루"));
        JsonNode docs = response.get("docs");
        if (docs == null || !docs.isArray()) throw new BookApiException("정보나루 도서 목록이 올바르지 않습니다.");
        Map<String, ExternalBook> result = new LinkedHashMap<>();
        for (JsonNode wrapper : docs) {
            JsonNode doc = wrapper.get("doc");
            if (doc == null) continue;
            ExternalBook book = parse(doc);
            if (!book.isbn13().isBlank() && !book.title().isBlank()) result.putIfAbsent(book.isbn13(), book);
        }
        return List.copyOf(result.values());
    }
    /** ISBN 상세 응답의 detail/book 구조에서 요청한 ISBN에 해당하는 결과를 찾는다. */
    public Optional<ExternalBook> details(String isbn) {
        String normalized = Isbn.normalize(isbn);
        if (normalized.isBlank()) return Optional.empty();
        var uri = builder("srchDtlList").queryParam("isbn13", normalized).queryParam("loaninfoYN", "N")
                .build().encode().toUri();
        JsonNode response = response(http.get(uri, null, "정보나루"));
        JsonNode detail = response.get("detail");
        if (detail == null || !detail.isArray()) return Optional.empty();
        for (JsonNode wrapper : detail) {
            JsonNode book = wrapper.get("book");
            if (book != null) {
                ExternalBook parsed = parse(book);
                if (normalized.equals(parsed.isbn13())) return Optional.of(parsed);
            }
        }
        return Optional.empty();
    }
    /** 인증키와 JSON 형식 매개변수를 구성한다. 이 URI를 로그나 오류 메시지에 출력하지 않는다. */
    private UriComponentsBuilder builder(String path) {
        if (config.getLibraryKey().isBlank()) throw new BookApiException("DATA4LIBRARY_API_KEY 설정이 필요합니다.");
        return UriComponentsBuilder.fromUriString(config.getLibraryUrl()).path("/" + path)
                .queryParam("authKey", config.getLibraryKey()).queryParam("format", "json");
    }
    /** HTTP 성공과 별개로 공급자가 반환한 오류 필드를 확인한다. */
    private JsonNode response(JsonNode root) {
        JsonNode response = root.get("response");
        if (response == null || !text(response, "error").isBlank())
            throw new BookApiException("정보나루 도서 API가 요청을 처리하지 못했습니다.");
        return response;
    }
    /** 누락·잘못된 대출량은 0으로 처리하고 KDC를 Tome의 대표 장르로 연결한다. */
    private ExternalBook parse(JsonNode book) {
        long count;
        try { count = Math.max(0, Long.parseLong(text(book, "loan_count"))); }
        catch (NumberFormatException e) { count = 0; }
        return new ExternalBook(Isbn.normalize(text(book, "isbn13")), text(book, "bookname"), text(book, "authors"),
                safeUrl(text(book, "bookImageURL")), safeUrl(text(book, "bookDtlUrl")), text(book, "publisher"),
                text(book, "description"), taxonomy.fromKdc(text(book, "class_no")), count, "DATA4LIBRARY");
    }
}
