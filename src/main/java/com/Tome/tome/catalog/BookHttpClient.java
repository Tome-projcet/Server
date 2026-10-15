package com.Tome.tome.catalog;

import com.Tome.tome.config.BookApiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class BookHttpClient {
    private final BookApiConfig config;
    private final ObjectMapper mapper = new ObjectMapper();
    public JsonNode get(URI uri, String authorization, String provider) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json").GET();
        if (authorization != null) builder.header("Authorization", authorization);
        try {
            HttpResponse<String> response = config.getHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) throw new BookApiException(provider + " 도서 API 응답 오류 (" + response.statusCode() + ")");
            return mapper.readTree(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BookApiException(provider + " 도서 API 요청이 중단되었습니다.");
        } catch (IOException e) {
            throw new BookApiException(provider + " 도서 API에 연결할 수 없습니다.");
        } catch (BookApiException e) {
            throw e;
        } catch (RuntimeException e) {
            // Never include raw request URLs, API keys or upstream response bodies.
            throw new BookApiException(provider + " 도서 API 응답 형식이 올바르지 않습니다.");
        }
    }
    public static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText().trim();
    }
    public static String safeUrl(String value) {
        if (value == null || value.isBlank()) return "";
        try {
            URI uri = URI.create(value);
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null ? value : "";
        } catch (IllegalArgumentException e) { return ""; }
    }
}
