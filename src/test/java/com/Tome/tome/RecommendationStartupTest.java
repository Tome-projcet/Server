package com.Tome.tome;

import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.net.URI;
import java.net.http.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** 실제 내장 HTTP 서버에서 현재 JWT 로그인·추천·인생 책·문서 계약을 검증한다. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
 properties="spring.datasource.url=jdbc:h2:mem:current-smoke;MODE=PostgreSQL;NON_KEYWORDS=SECOND;DB_CLOSE_DELAY=-1")
class RecommendationStartupTest {
 @Autowired Environment env;
 @Autowired UserRepository users;
 @Autowired PasswordEncoder passwords;
 @Autowired com.Tome.tome.domain.book.service.BookCatalogStore catalog;
 final HttpClient client=HttpClient.newHttpClient();
 final tools.jackson.databind.ObjectMapper mapper=new tools.jackson.databind.ObjectMapper();
 String base, token, refresh;
 Long userId;
 HttpResponse<String> send(HttpRequest.Builder r) throws Exception {return client.send(r.build(),HttpResponse.BodyHandlers.ofString());}
 HttpRequest.Builder req(String path) {return HttpRequest.newBuilder(URI.create(base+path));}
 HttpRequest.Builder auth(String path) {return req(path).header("Authorization","Bearer "+token);}
 @BeforeEach void loginWithCurrentJwtContract() throws Exception {
  base="http://127.0.0.1:"+env.getProperty("local.server.port");
  var csrf=send(req("/api/csrf").GET()); assertEquals(200,csrf.statusCode());
  String cookie=csrf.headers().allValues("Set-Cookie").stream().filter(c->c.startsWith("XSRF-TOKEN=")).findFirst().orElseThrow().split(";",2)[0];
  String email=UUID.randomUUID()+"@example.org";
  userId=users.save(User.builder().email(email).nickname(UUID.randomUUID().toString()).password(passwords.encode("test-password")).build()).getId();
  var login=send(req("/login").header("Cookie",cookie).header("X-XSRF-TOKEN",mapper.readTree(csrf.body()).path("token").asText())
   .header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString("email="+email+"&password=test-password")));
  assertEquals(200,login.statusCode());
  token=mapper.readTree(login.body()).path("accessToken").asText();assertFalse(token.isBlank());
  refresh=login.headers().allValues("Set-Cookie").stream().filter(c->c.startsWith("refresh_token=")).findFirst().orElseThrow().split(";",2)[0];
 }
 @Test void currentLoginRefreshRecommendationGenresAndSwaggerWork() throws Exception {
  assertEquals(401,send(req("/api/recommend/books").GET()).statusCode());
  assertEquals(401,send(req("/api/mypage/lifebook").GET()).statusCode());
  assertEquals(200,send(auth("/api/recommend/books").GET()).statusCode());
  var genres=send(auth("/api/genres").GET());assertEquals(200,genres.statusCode());assertTrue(genres.body().contains("소설"));
  var prefs=send(auth("/api/genres/preferences").header("Content-Type","application/json").PUT(HttpRequest.BodyPublishers.ofString("{\"names\":[\"소설\"]}")));
  assertEquals(200,prefs.statusCode());
  assertEquals(200,send(auth("/api/genres/preferences").GET()).statusCode());
  var renewed=send(req("/api/token").header("Cookie",refresh).POST(HttpRequest.BodyPublishers.noBody()));
  assertEquals(201,renewed.statusCode());assertFalse(mapper.readTree(renewed.body()).path("accessToken").asText().isBlank());
  // 실제 JWT 요청으로 인생 책을 설정하고 마이페이지와 다른 사용자 페이지 반영을 확인한다.
  assertEquals(204,send(auth("/api/mypage/lifebook").GET()).statusCode());
  catalog.upsert(java.util.List.of(new com.Tome.tome.domain.book.infra.ExternalBook("9788983921987", "인생 책", "작가", "", "", "출판사", "소개", "소설", 0, "KAKAO")));
  var selected=send(auth("/api/onbording/book").header("Content-Type","application/json").PUT(HttpRequest.BodyPublishers.ofString("{\"isbn\":\"9788983921987\"}")));
  assertEquals(200,selected.statusCode());
  var mine=send(auth("/api/mypage").GET());assertEquals(200,mine.statusCode());
  assertEquals("9788983921987",mapper.readTree(mine.body()).path("lifeBook").path("isbn13").asText());
  assertEquals(200,send(auth("/api/mypage/lifebook").GET()).statusCode());
  var otherProfile=send(auth("/api/otherpage/"+userId).GET());assertEquals(200,otherProfile.statusCode());
  assertEquals("9788983921987",mapper.readTree(otherProfile.body()).path("lifeBook").path("isbn13").asText());
  var invalid=send(auth("/api/mypage/lifebook").header("Content-Type","application/json").PUT(HttpRequest.BodyPublishers.ofString("{\"isbn\":\"wrong\"}")));
  assertEquals(400,invalid.statusCode());
  assertEquals(204,send(auth("/api/mypage/lifebook").DELETE()).statusCode());
  assertEquals(204,send(auth("/api/mypage/lifebook").GET()).statusCode());
  assertEquals(200,send(req("/swagger-ui/index.html").GET()).statusCode());
  var docs=send(req("/v3/api-docs/books-and-recommendations").GET());
  assertEquals(200,docs.statusCode());
  var spec=mapper.readTree(docs.body());
  assertTrue(spec.path("paths").has("/api/recommend/books"));
  assertTrue(spec.path("paths").has("/api/mypage/lifebook"));
  assertTrue(spec.path("paths").has("/api/onbording/book"));
  var parameters=spec.path("paths").path("/api/mypage/lifebook").path("put").path("parameters");
  assertTrue(parameters.isMissingNode() || parameters.isEmpty());
  assertTrue(spec.path("components").path("securitySchemes").has("bearerAuth"));
  assertFalse(spec.path("components").path("securitySchemes").has("loginSession"));
 }
 @Test void forbiddenRecommendationAndSyncMustReturn403() throws Exception {
  var other=send(auth("/api/recommend/books?userId=999999999").GET());
  var sync=send(auth("/api/books/sync/popular").POST(HttpRequest.BodyPublishers.noBody()));
  assertAll(()->assertEquals(403,other.statusCode(),"다른 사용자 추천 조회"),()->assertEquals(403,sync.statusCode(),"동기화 토큰 누락"));
 }
}
