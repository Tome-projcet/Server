package com.Tome.tome;

import com.Tome.tome.domain.book.infra.*;
import com.Tome.tome.domain.book.repository.BookRepository;
import com.Tome.tome.domain.book.service.*;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.repository.UserRepository;
import com.Tome.tome.domain.user.service.LifeBookService;
import com.Tome.tome.global.config.jwt.TokenProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Duration;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 실제 보안 필터와 DB를 통과시켜 입력·JWT·사용자 격리·장애·브라우저 접근을 검증한다. */
@SpringBootTest(properties="spring.jpa.open-in-view=false")
@Transactional
class LifeBookHttpEdgeTest {
    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    @Autowired BookRepository books;
    @Autowired BookCatalogStore catalog;
    @Autowired LifeBookService lifeBooks;
    @Autowired TokenProvider tokens;
    @MockitoBean BookCatalogService external;
    MockMvc mvc;
    User user;
    String authorization;
    static final String ISBN="9788983921987";
    static final String OTHER="9788983921994";

    @BeforeEach void setup() {
        user=users.save(User.builder().email(UUID.randomUUID()+"@example.org").nickname("edge").password("hash").build());
        authorization="Bearer "+tokens.generateToken(user,Duration.ofMinutes(5),TokenProvider.TYPE_ACCESS);
        catalog.upsert(List.of(new ExternalBook(ISBN,"기존 책","작가","","","출판사","소개","소설",5,"KAKAO")));
        lifeBooks.set(user.getId(),ISBN);
        mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
    void assertPreserved() throws Exception {
        mvc.perform(get("/api/mypage/lifebook").header("Authorization",authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.isbn13").value(ISBN));
    }

    @ParameterizedTest
    @ValueSource(strings={"{}","null","{","{\"isbn\":null}","{\"isbn\":\"\"}","{\"isbn\":\"  \"}","{\"isbn\":\"9788983921988\"}","{\"isbn\":[\"9788983921987\"]}"})
    void malformedOrMissingIsbnReturns400WithoutLosingSelection(String body) throws Exception {
        mvc.perform(put("/api/mypage/lifebook").header("Authorization",authorization)
                        .contentType("application/json").content(body)).andExpect(status().isBadRequest());
        assertPreserved();verifyNoInteractions(external);
    }
    @Test void absentBodyAndWrongContentTypeAreClientErrors() throws Exception {
        mvc.perform(put("/api/mypage/lifebook").header("Authorization",authorization).contentType("application/json"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/mypage/lifebook").header("Authorization",authorization).contentType("text/plain").content(ISBN))
                .andExpect(status().isUnsupportedMediaType());
        assertPreserved();
    }
    @Test void missingInvalidExpiredAndRefreshTokensCannotChangeLifeBook() throws Exception {
        mvc.perform(get("/api/mypage/lifebook")).andExpect(status().isUnauthorized());
        String expired=tokens.generateToken(user,Duration.ofSeconds(-60),TokenProvider.TYPE_ACCESS);
        String refresh=tokens.generateToken(user,Duration.ofMinutes(5),TokenProvider.TYPE_REFRESH);
        for(String token:List.of("not-a-token",expired,refresh)) {
            mvc.perform(put("/api/mypage/lifebook").header("Authorization","Bearer "+token)
                            .contentType("application/json").content("{\"isbn\":\""+OTHER+"\"}"))
                    .andExpect(status().isUnauthorized());
            mvc.perform(delete("/api/mypage/lifebook").header("Authorization","Bearer "+token))
                    .andExpect(status().isUnauthorized());
        }
        assertPreserved();verifyNoInteractions(external);
    }
    @Test void tokenOfDeletedUserReturns401InsteadOf500() throws Exception {
        users.delete(user);users.flush();
        mvc.perform(get("/api/mypage/lifebook").header("Authorization",authorization)).andExpect(status().isUnauthorized());
    }
    @Test void userIdInBodyCannotChangeAnotherUsersSelection() throws Exception {
        var other=users.save(User.builder().email(UUID.randomUUID()+"@example.org").nickname("other").password("hash").build());
        mvc.perform(put("/api/mypage/lifebook").header("Authorization",authorization).contentType("application/json")
                        .content("{\"isbn\":\""+ISBN+"\",\"userId\":"+other.getId()+"}"))
                .andExpect(status().isOk());
        assertNull(users.findById(other.getId()).orElseThrow().getLifeBook());
        mvc.perform(delete("/api/mypage/lifebook").header("Authorization",authorization)).andExpect(status().isNoContent());
        assertTrue(books.findByIsbn13(ISBN).isPresent());
    }
    @Test void providerNotFoundAndUnavailablePreserveExistingLifeBook() throws Exception {
        when(external.details(OTHER)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,"책을 찾을 수 없습니다"));
        mvc.perform(put("/api/mypage/lifebook").header("Authorization",authorization).contentType("application/json")
                        .content("{\"isbn\":\""+OTHER+"\"}"))
                .andExpect(status().isNotFound());
        doThrow(new BookApiException("도서 API 이용 불가")).when(external).details(OTHER);
        mvc.perform(put("/api/mypage/lifebook").header("Authorization",authorization).contentType("application/json")
                        .content("{\"isbn\":\""+OTHER+"\"}"))
                .andExpect(status().isServiceUnavailable());
        assertPreserved();assertEquals(1,books.count());
    }
    @Test void mismatchedProviderIsbnIsRejectedWithoutChangingUserOrCatalog() throws Exception {
        when(external.details(OTHER)).thenReturn(new ExternalBook(ISBN,"다른 책","","","","","","인문",0,"KAKAO"));
        mvc.perform(put("/api/mypage/lifebook").header("Authorization",authorization).contentType("application/json")
                        .content("{\"isbn\":\""+OTHER+"\"}"))
                .andExpect(status().isServiceUnavailable());
        assertPreserved();assertEquals("기존 책",books.findByIsbn13(ISBN).orElseThrow().getTitle());
    }
    @Test void repeatedSelectionAndHyphenatedIsbnReuseTheSameRow() throws Exception {
        var id=books.findByIsbn13(ISBN).orElseThrow().getId();
        for(String isbn:List.of(ISBN,"8983921986","978-89-8392-198-7",ISBN))
            mvc.perform(put("/api/mypage/lifebook").header("Authorization",authorization).contentType("application/json")
                            .content("{\"isbn\":\""+isbn+"\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        assertEquals(1,books.count());verifyNoInteractions(external);
    }
    @Test void corsAllowsLifeBookAndSyncHeadersOnlyForConfiguredOrigins() throws Exception {
        mvc.perform(options("/api/mypage/lifebook").header("Origin","http://localhost:5173")
                        .header("Access-Control-Request-Method","PUT").header("Access-Control-Request-Headers","authorization,content-type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
        mvc.perform(options("/api/mypage/lifebook").header("Origin","https://untrusted.example")
                        .header("Access-Control-Request-Method","PUT"))
                .andExpect(status().isForbidden());
        mvc.perform(options("/api/books/sync/popular").header("Origin","http://localhost:5173")
                        .header("Access-Control-Request-Method","POST").header("Access-Control-Request-Headers","authorization,x-book-sync-token"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Headers",org.hamcrest.Matchers.containsString("x-book-sync-token")));
    }
}
