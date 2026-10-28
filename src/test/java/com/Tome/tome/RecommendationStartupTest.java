package com.Tome.tome;

import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.net.URI;
import java.net.http.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** 외부 API 없이 실제 서버·로그인 세션·문서 화면과 생성된 OpenAPI 설명을 함께 검증한다. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:tome-startup;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
class RecommendationStartupTest {
    @Autowired Environment environment;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    /** 기본 추천·장르 API와 Swagger 문서가 같은 로그인 세션으로 동작하는지 확인한다. */
    @Test void serverStartsWithoutApiKeysAndAuthenticatedRecommendationWorks() throws Exception {
        String base = "http://127.0.0.1:" + environment.getProperty("local.server.port");
        var http = HttpClient.newHttpClient();
        var unauthenticated = http.send(HttpRequest.newBuilder(URI.create(base + "/api/recommend/books")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(302, unauthenticated.statusCode());
        // main의 쿠키 기반 CSRF 정책을 따라 로그인에도 토큰을 전달한다.
        String csrfCookie = unauthenticated.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith("XSRF-TOKEN=")).findFirst().orElseThrow().split(";", 2)[0];
        String csrf = csrfCookie.substring("XSRF-TOKEN=".length());
        String email = UUID.randomUUID() + "@example.org";
        users.save(User.builder().email(email).nickname("startup-reader").password(passwords.encode("test-password")).build());
        var login = http.send(HttpRequest.newBuilder(URI.create(base + "/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", csrfCookie).header("X-XSRF-TOKEN", csrf)
                .POST(HttpRequest.BodyPublishers.ofString("email=" + email + "&password=test-password")).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(302, login.statusCode());
        assertTrue(login.headers().firstValue("Location").orElseThrow().endsWith("/main"));
        String cookie = login.headers().allValues("Set-Cookie").stream().filter(c -> c.startsWith("JSESSIONID="))
                .findFirst().orElseThrow().split(";", 2)[0];
        var recommendations = http.send(HttpRequest.newBuilder(URI.create(base + "/api/recommend/books"))
                .header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, recommendations.statusCode());
        assertEquals("[]", recommendations.body());
        var genres = http.send(HttpRequest.newBuilder(URI.create(base + "/api/genres"))
                .header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, genres.statusCode());
        assertTrue(genres.body().contains("소설"));

        // 로그인 때 main이 발급한 refresh_token으로 JWT를 얻어 새 User 타입 주입도 검증한다.
        String refreshCookie = login.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith("refresh_token=")).findFirst().orElseThrow().split(";", 2)[0];
        var tokenResponse = http.send(HttpRequest.newBuilder(URI.create(base + "/api/token"))
                .header("Cookie", refreshCookie).POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(201, tokenResponse.statusCode());
        var mapper = new tools.jackson.databind.ObjectMapper();
        String accessToken = mapper.readTree(tokenResponse.body()).path("accessToken").asText();
        assertFalse(accessToken.isBlank());
        var jwtRecommendations = http.send(HttpRequest.newBuilder(URI.create(base + "/api/recommend/books"))
                .header("Authorization", "Bearer " + accessToken).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, jwtRecommendations.statusCode());
        var noCsrf = http.send(HttpRequest.newBuilder(URI.create(base + "/api/genres/preferences"))
                .header("Cookie", cookie).header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"names\":[\"소설\"]}")).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(403, noCsrf.statusCode());
        String newCsrfCookie = genres.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith("XSRF-TOKEN=")).findFirst().orElseThrow().split(";", 2)[0];
        var csrfPreferences = http.send(HttpRequest.newBuilder(URI.create(base + "/api/genres/preferences"))
                .header("Cookie", cookie + "; " + newCsrfCookie)
                .header("X-XSRF-TOKEN", newCsrfCookie.substring("XSRF-TOKEN=".length()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"names\":[\"소설\"]}")).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, csrfPreferences.statusCode());
        var jwtPreferences = http.send(HttpRequest.newBuilder(URI.create(base + "/api/genres/preferences"))
                .header("Authorization", "Bearer " + accessToken).header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"names\":[\"인문\"]}")).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, jwtPreferences.statusCode());

        // 문서도 기존 보안 정책을 유지한다. 로그인하지 않으면 문서 내용을 공개하지 않는다.
        var protectedDocs = http.send(HttpRequest.newBuilder(URI.create(base + "/v3/api-docs/books-and-recommendations"))
                .GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(302, protectedDocs.statusCode());
        var docs = http.send(HttpRequest.newBuilder(URI.create(base + "/v3/api-docs/books-and-recommendations"))
                .header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, docs.statusCode());
        var json = new tools.jackson.databind.ObjectMapper().readTree(docs.body());
        assertEquals(6, json.path("paths").size());
        var recommendation = json.path("paths").path("/api/recommend/books").path("get");
        assertEquals("내 취향에 맞는 도서 추천", recommendation.path("summary").asText());
        assertTrue(recommendation.path("description").asText().contains("75%"));
        assertEquals(2, recommendation.path("parameters").size()); // 서버가 주입하는 User는 숨긴다.
        assertTrue(json.path("components").path("securitySchemes").has("bearerAuth"));
        var schemas = json.path("components").path("schemas");
        assertTrue(schemas.path("BookResponseDto").path("properties").path("recommendationReason")
                .path("description").asText().contains("근거"));
        assertTrue(schemas.path("ExternalBook").path("properties").path("genre")
                .path("description").asText().contains("기타"));
        var request = schemas.path("PreferenceRequest");
        assertEquals("names", request.path("required").get(0).asText());
        var sync = json.path("paths").path("/api/books/sync/popular").path("post");
        assertTrue(sync.path("description").asText().contains("20권"));
        assertTrue(sync.path("responses").has("403"));
        boolean tokenDocumented = false;
        for (var parameter : sync.path("parameters")) {
            if ("X-Book-Sync-Token".equals(parameter.path("name").asText())) {
                tokenDocumented = true;
                assertTrue(parameter.path("required").asBoolean());
                assertEquals("password", parameter.path("schema").path("format").asText());
            }
        }
        assertTrue(tokenDocumented);
        // 실제 Swagger UI 문서와 배포된 화면 리소스가 모두 응답하는지도 확인한다.
        var ui = http.send(HttpRequest.newBuilder(URI.create(base + "/swagger-ui/index.html"))
                .header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, ui.statusCode());
        assertTrue(ui.body().contains("swagger-ui"));
        var uiConfig = http.send(HttpRequest.newBuilder(URI.create(base + "/v3/api-docs/swagger-config"))
                .header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, uiConfig.statusCode());
        var initializer = http.send(HttpRequest.newBuilder(URI.create(base + "/swagger-ui/swagger-initializer.js"))
                .header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, initializer.statusCode());
        assertTrue(initializer.body().contains("X-XSRF-TOKEN"), "Swagger initializer must send CSRF header");
        assertTrue(initializer.body().contains("XSRF-TOKEN"), "Swagger initializer must read CSRF cookie");
    }
}
