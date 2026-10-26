package com.Tome.tome.domain.book.infra;

import com.Tome.tome.global.config.BookApiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

/** 도서 API 공통 HTTP 처리. 연결 3초·요청 5초 제한을 두고 민감한 주소와 응답을 오류에 포함하지 않는다. */
@Component
@RequiredArgsConstructor
public class BookHttpClient {
    private final BookApiConfig config;
    private final ObjectMapper mapper = new ObjectMapper();
    /** 지정된 공급자에 JSON을 요청한다. HTTP 오류·타임아웃·중단은 사용자용 예외로 변환한다. */
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
            // URI에 포함된 정보나루 인증키나 공급자의 원문 응답을 예외 메시지로 노출하지 않는다.
            throw new BookApiException(provider + " 도서 API 응답 형식이 올바르지 않습니다.");
        }
    }
    /** JSON 필드가 없거나 null이면 빈 문자열을 반환해 공급자별 누락 필드를 처리한다. */
    public static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText().trim();
    }
    /** 화면 링크에는 호스트가 있는 HTTP/HTTPS 주소만 허용한다. 반환 주소를 서버가 다시 호출하지는 않는다. */
    public static String safeUrl(String value) {
        if (value == null || value.isBlank()) return "";
        try {
            URI uri = URI.create(value);
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null ? value : "";
        } catch (IllegalArgumentException e) { return ""; }
    }
}
