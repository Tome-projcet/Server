package com.Tome.tome.catalog;

import com.Tome.tome.config.BookApiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.*;
import static com.Tome.tome.catalog.BookHttpClient.*;

@Component
@RequiredArgsConstructor
public class LibraryBookClient {
    private final BookApiConfig config;
    private final BookHttpClient http;
    private final GenreTaxonomy taxonomy;
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
    private UriComponentsBuilder builder(String path) {
        if (config.getLibraryKey().isBlank()) throw new BookApiException("DATA4LIBRARY_API_KEY 설정이 필요합니다.");
        return UriComponentsBuilder.fromUriString(config.getLibraryUrl()).path("/" + path)
                .queryParam("authKey", config.getLibraryKey()).queryParam("format", "json");
    }
    private JsonNode response(JsonNode root) {
        JsonNode response = root.get("response");
        if (response == null || !text(response, "error").isBlank())
            throw new BookApiException("정보나루 도서 API가 요청을 처리하지 못했습니다.");
        return response;
    }
    private ExternalBook parse(JsonNode book) {
        long count;
        try { count = Math.max(0, Long.parseLong(text(book, "loan_count"))); }
        catch (NumberFormatException e) { count = 0; }
        return new ExternalBook(Isbn.normalize(text(book, "isbn13")), text(book, "bookname"), text(book, "authors"),
                safeUrl(text(book, "bookImageURL")), safeUrl(text(book, "bookDtlUrl")), text(book, "publisher"),
                text(book, "description"), taxonomy.fromKdc(text(book, "class_no")), count, "DATA4LIBRARY");
    }
}
