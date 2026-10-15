package com.Tome.tome;

import com.Tome.tome.domain.User;
import com.Tome.tome.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.net.URI;
import java.net.http.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises a real embedded server and a form-login session, without external APIs. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:tome-startup;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
class RecommendationStartupTest {
    @Autowired Environment environment;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Test void serverStartsWithoutApiKeysAndAuthenticatedRecommendationWorks() throws Exception {
        String base = "http://127.0.0.1:" + environment.getProperty("local.server.port");
        var http = HttpClient.newHttpClient();
        var unauthenticated = http.send(HttpRequest.newBuilder(URI.create(base + "/api/recommend/books")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(302, unauthenticated.statusCode());
        String email = UUID.randomUUID() + "@example.org";
        users.save(User.builder().email(email).nickname("startup-reader").password(passwords.encode("test-password")).build());
        var login = http.send(HttpRequest.newBuilder(URI.create(base + "/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
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
    }
}
