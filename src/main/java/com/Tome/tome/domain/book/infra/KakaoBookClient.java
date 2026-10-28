package com.Tome.tome.domain.book.infra;

import com.Tome.tome.global.config.BookApiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import java.util.*;
import static com.Tome.tome.domain.book.infra.BookHttpClient.*;

/** 카카오 도서 검색 응답을 공통 도서 형식으로 변환한다. 장르는 제공되지 않으므로 기타로 둔다. */
@Component
@RequiredArgsConstructor
public class KakaoBookClient {
    private final BookApiConfig config;
    private final BookHttpClient http;
    /** 사용자 검색어와 페이지 조건으로 검색한다. 매개변수 범위는 컨트롤러에서 검사한다. */
    public List<ExternalBook> search(String query, int page, int size) {
        return request(query, null, page, size);
    }
    /** ISBN 검색 결과 중 요청 ISBN과 정확히 같은 책만 선택한다. */
    public Optional<ExternalBook> findByIsbn(String isbn) {
        String normalized = Isbn.normalize(isbn);
        if (normalized.isBlank()) return Optional.empty();
        return request(normalized, "isbn", 1, 10).stream()
                .filter(book -> normalized.equals(book.isbn13())).findFirst();
    }
    /** 인증 헤더를 구성하고 검색어를 인코딩한다. 유효하지 않은 ISBN과 응답 내 중복 책은 제외한다. */
    private List<ExternalBook> request(String query, String target, int page, int size) {
        if (config.getKakaoKey().isBlank()) throw new BookApiException("KAKAO_REST_API_KEY 설정이 필요합니다.");
        var builder = UriComponentsBuilder.fromUriString(config.getKakaoUrl())
                .queryParam("query", "{query}").queryParam("page", page).queryParam("size", size);
        if (target != null) builder.queryParam("target", target);
        var root = http.get(builder.encode().buildAndExpand(query).toUri(), "KakaoAK " + config.getKakaoKey(), "카카오");
        JsonNode documents = root.get("documents");
        if (documents == null || !documents.isArray()) throw new BookApiException("카카오 도서 목록이 올바르지 않습니다.");
        Map<String, ExternalBook> result = new LinkedHashMap<>();
        for (JsonNode item : documents) {
            String isbn = Isbn.normalize(text(item, "isbn"));
            if (isbn.isBlank() || text(item, "title").isBlank()) continue;
            List<String> authors = new ArrayList<>();
            JsonNode authorNode = item.get("authors");
            if (authorNode != null && authorNode.isArray()) authorNode.forEach(a -> authors.add(a.asText()));
            result.putIfAbsent(isbn, new ExternalBook(isbn, text(item, "title"), String.join(", ", authors),
                    safeUrl(text(item, "thumbnail")), safeUrl(text(item, "url")), text(item, "publisher"),
                    text(item, "contents"), "기타", 0, "KAKAO"));
        }
        return List.copyOf(result.values());
    }
}
