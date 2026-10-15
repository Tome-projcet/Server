package com.Tome.tome.catalog;

import com.Tome.tome.config.BookApiConfig;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class BookClientsTest {
    private HttpServer server;
    private BookApiConfig config;
    private BookHttpClient http;
    private String base;
    private final AtomicReference<String> query = new AtomicReference<>();
    private final AtomicReference<String> auth = new AtomicReference<>();
    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
        config = new BookApiConfig();
        ReflectionTestUtils.setField(config, "kakaoKey", "test-kakao-key");
        ReflectionTestUtils.setField(config, "libraryKey", "test-library-key");
        ReflectionTestUtils.setField(config, "kakaoUrl", base + "/search");
        ReflectionTestUtils.setField(config, "libraryUrl", base + "/api");
        http = new BookHttpClient(config);
    }
    @AfterEach void stop() { server.stop(0); }
    private void response(String path, int status, String json) {
        server.createContext(path, exchange -> {
            query.set(exchange.getRequestURI().getRawQuery());
            auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(status, body.length);
            try (var out = exchange.getResponseBody()) { out.write(body); }
        });
    }
    @Test void kakaoMapsSearchAndEncodesQueryAsOneParameter() {
        response("/search", 200, """
                {"documents":[{"isbn":"8983921986 9788983921987","title":"해리 포터",
                "authors":["작가 A","작가 B"],"thumbnail":"https://example.org/cover.jpg",
                "url":"javascript:alert(1)","publisher":"출판사","contents":"책 소개"},
                {"isbn":"9788983921987","title":"중복"},{"isbn":"bad","title":"잘못된 ISBN"}]}
                """);
        var books = new KakaoBookClient(config, http).search("소설 & 철학", 1, 10);
        assertEquals(1, books.size());
        assertEquals("9788983921987", books.getFirst().isbn13());
        assertEquals("작가 A, 작가 B", books.getFirst().writer());
        assertEquals("", books.getFirst().detailUrl());
        assertEquals("KakaoAK test-kakao-key", auth.get());
        assertTrue(query.get().contains("%26"));
        assertEquals(3, query.get().split("&").length);
    }
    @Test void libraryParsesWrappedBooksKdcAndStringCounts() {
        response("/api/loanItemSrch", 200, """
                {"response":{"docs":[{"doc":{"isbn13":"9788983921987","bookname":"소설",
                "authors":"작가","publisher":"출판사","class_no":"813.7","loan_count":"123",
                "bookImageURL":"https://example.org/cover.jpg","bookDtlUrl":"https://example.org/book"}}]}}
                """);
        var books = new LibraryBookClient(config, http, new GenreTaxonomy()).popular(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), 10);
        assertEquals("소설", books.getFirst().genre());
        assertEquals(123, books.getFirst().loanCount());
        assertTrue(query.get().contains("format=json"));
        assertTrue(query.get().contains("authKey=test-library-key"));
    }
    @Test void libraryDetailsParsesBookWrapper() {
        response("/api/srchDtlList", 200, """
                {"response":{"detail":[{"book":{"isbn13":"9788983921987","bookname":"상세",
                "class_no":"325.1","description":"소개"}}]}}
                """);
        var book = new LibraryBookClient(config, http, new GenreTaxonomy()).details("9788983921987").orElseThrow();
        assertEquals("경제/경영", book.genre());
        assertEquals("소개", book.description());
    }
    @Test void upstreamFailuresDoNotLeakCredentials() {
        response("/api/loanItemSrch", 429, "secret test-library-key");
        var client = new LibraryBookClient(config, http, new GenreTaxonomy());
        var error = assertThrows(BookApiException.class, () -> client.popular(LocalDate.now(), LocalDate.now(), 10));
        assertFalse(error.getMessage().contains("test-library-key"));
        assertTrue(error.getMessage().contains("429"));
    }
    @Test void providerApplicationErrorsAreNotSuccessfulEmptyLists() {
        response("/api/loanItemSrch", 200, """
                {"response":{"error":"invalid auth key test-library-key"}}
                """);
        var client = new LibraryBookClient(config, http, new GenreTaxonomy());
        assertThrows(BookApiException.class, () -> client.popular(LocalDate.now(), LocalDate.now(), 10));
    }
    @Test void missingKeyFailsBeforeRequest() {
        ReflectionTestUtils.setField(config, "kakaoKey", "");
        assertThrows(BookApiException.class, () -> new KakaoBookClient(config, http).search("책", 1, 10));
        assertNull(query.get());
    }
    @Test void isbnNormalizesTenDigitAndChecksChecksums() {
        assertEquals("9788983921987", Isbn.normalize("8983921986"));
        assertEquals("9788983921987", Isbn.normalize("978-89-8392-198-7"));
        assertEquals("", Isbn.normalize("9788983921988"));
        assertEquals("", Isbn.normalize("1234567890"));
    }
    @Test void taxonomyMapsKdcAndLegacyNames() {
        var taxonomy = new GenreTaxonomy();
        assertEquals("소설", taxonomy.fromKdc("813.7"));
        assertEquals("시/에세이", taxonomy.fromKdc("811.6"));
        assertEquals("생활/건강", taxonomy.fromKdc("598.1"));
        assertEquals("인문", taxonomy.fromKdc("180"));
        assertEquals("기타", taxonomy.fromKdc(""));
        assertEquals("경제/경영", taxonomy.canonical("경제경영"));
    }
}
