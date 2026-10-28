package com.Tome.tome;

import com.Tome.tome.domain.book.infra.*;
import com.Tome.tome.global.config.BookApiConfig;
import com.Tome.tome.domain.book.controller.*;
import com.Tome.tome.domain.genre.controller.*;
import com.Tome.tome.domain.book.entity.Book;
import com.Tome.tome.domain.genre.entity.Genre;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.entity.UserGenre;
import com.Tome.tome.domain.book.repository.BookRepository;
import com.Tome.tome.domain.genre.repository.GenreRepository;
import com.Tome.tome.domain.user.repository.UserRepository;
import com.Tome.tome.domain.user.repository.UserGenreRepository;
import com.Tome.tome.domain.book.service.*;
import com.Tome.tome.domain.genre.service.GenrePreferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@Transactional
class RecommendationIntegrationTests {
    @Autowired BookCatalogStore catalog;
    @Autowired BookRepository books;
    @Autowired GenreRepository genres;
    @Autowired UserRepository users;
    @Autowired UserGenreRepository preferences;
    @Autowired RecommendationService recommendation;
    @Autowired GenrePreferenceService preferenceService;
    @Autowired WebApplicationContext context;
    @Autowired com.Tome.tome.domain.user.service.UserService mainUserService;
    @Autowired com.Tome.tome.domain.user.service.UserMostService mainUserMostService;
    @Autowired com.Tome.tome.domain.user.repository.UserMostGenreRepository mostGenres;
    private User user() {
        return users.save(User.builder().email(UUID.randomUUID()+"@example.org").nickname("reader").password("hash").build());
    }
    private ExternalBook book(int index, String genre, long count) {
        String prefix = "978000000" + String.format("%03d", index);
        int sum = 0;
        for (int i = 0; i < 12; i++) sum += (prefix.charAt(i)-'0') * (i%2==0?1:3);
        return new ExternalBook(prefix+(10-sum%10)%10, "책"+index, "작가", "", "", "출판사", "소개", genre, count, "DATA4LIBRARY");
    }
    @Test void repeatedSyncUpdatesInsteadOfDuplicatingAndSeedsStableGenres() {
        assertEquals(13, catalog.ensureGenres().size());
        assertEquals(13, catalog.ensureGenres().size());
        var first = book(1,"경제경영",20);
        catalog.upsert(List.of(first));
        var id = books.findByIsbn13(first.isbn13()).orElseThrow().getId();
        catalog.upsert(List.of(new ExternalBook(first.isbn13(), "갱신된 제목", "작가", "https://example.org/new.jpg", "", "출판사", "새 소개", "경제/경영", 99,"DATA4LIBRARY+KAKAO")));
        books.flush();
        var updated = books.findByIsbn13(first.isbn13()).orElseThrow();
        assertEquals(1, books.count()); assertEquals(id,updated.getId());
        assertEquals(99,updated.getLoanCount()); assertEquals("경제/경영",updated.getGenre().getName());
        assertEquals("갱신된 제목",updated.getTitle());
    }
    @Test void emptyPreferencesGetPopularFallbackAndEmptyCatalogIsValid() {
        var user = user();
        assertTrue(recommendation.getRecommendations(user.getId()).isEmpty());
        catalog.upsert(List.of(book(1,"소설",5),book(2,"인문",100)));
        var result= recommendation.getRecommendations(user.getId());
        assertEquals(2,result.size()); assertEquals("책2",result.getFirst().getTitle());
        assertNotNull(result.getFirst().getRecommendationReason());
    }
    @Test void preferenceAliasesDiversifyAndLegacyZeroCountsStillMeanInterest() {
        var user=user();
        preferences.save(new UserGenre("경제경영", user));
        List<ExternalBook> records=new ArrayList<>();
        for(int i=1;i<=12;i++) records.add(book(i,"경제/경영",100-i));
        for(int i=13;i<=18;i++) records.add(book(i,"소설",10));
        catalog.upsert(records);
        var result=recommendation.getRecommendations(user.getId());
        assertEquals(10,result.size());
        assertEquals(4,result.stream().filter(b->b.getGenre().equals("경제/경영")).count());
        assertEquals(10,result.stream().map(b->b.getIsbn13()).distinct().count());
        assertTrue(result.getFirst().getRecommendationReason().contains("경제/경영"));
    }
    @Test void oneGenreCanFillAllTenSlots() {
        var user=user();
        List<ExternalBook> records=new ArrayList<>();
        for(int i=1;i<=12;i++) records.add(book(i,"소설",20));
        catalog.upsert(records);
        assertEquals(10,recommendation.getRecommendations(user.getId()).size());
    }
    @Test void preferenceReplacementValidatesAndPreservesExistingCounts() {
        var user=user();
        var genre=new UserGenre("경제경영",user);genre.increaseCount();genre.increaseCount();preferences.save(genre);
        var result=preferenceService.replace(user.getId(),List.of("경제/경영","소설","소설"));
        assertEquals(2,result.size());
        assertEquals(2,result.stream().filter(p->p.name().equals("경제/경영")).findFirst().orElseThrow().count());
        assertThrows(IllegalArgumentException.class,()->preferenceService.replace(user.getId(),List.of("없는장르")));
    }
    @Test void catalogFallbackWorksWithoutExternalKeys() {
        var record = book(1, "소설", 5);
        catalog.upsert(List.of(record));
        var service = context.getBean(BookCatalogService.class);
        assertEquals(record.isbn13(), service.search("책1", 1, 10).getFirst().isbn13());
        assertEquals(record.isbn13(), service.details(record.isbn13()).isbn13());
    }
    @Test void longMetadataCanBePersisted() {
        var original = book(1, "소설", 5);
        var record = new ExternalBook(original.isbn13(), "제목".repeat(200), "작가".repeat(300),
                "https://example.org/"+"a".repeat(600), "https://example.org/"+"b".repeat(600),
                "출판사", "소개".repeat(3000), "소설", 5, "KAKAO");
        catalog.upsert(List.of(record)); books.flush();
        assertEquals(4000, books.findByIsbn13(record.isbn13()).orElseThrow().getDescription().length());
    }
    /** main의 장르 기록·상위 장르 계산과 관심사 교체를 연속 실행해 공유 데이터 계약을 확인한다. */
    @Test void mainGenreTrackingAndRecommendationShareTheSamePreferences() {
        var user = user();
        mainUserService.checkGenre(user, "경제경영");
        mainUserService.checkGenre(user, "경제경영");
        preferenceService.replace(user.getId(), List.of("경제/경영", "소설"));
        mainUserService.checkGenre(user, "경제/경영");
        mainUserMostService.mostGenreset(user);
        preferences.flush();
        assertEquals(2, preferences.findByUserAndName(user, "경제/경영").orElseThrow().getCount());
        assertEquals("경제/경영", mostGenres.findByUser_Id(user.getId()).orElseThrow().getFirst());
        catalog.upsert(List.of(book(1, "경제/경영", 1), book(2, "소설", 1)));
        assertEquals("경제/경영", recommendation.getRecommendations(user.getId(), 1).getFirst().getGenre());
    }
    /** main의 페이지 기반 도서 조회가 새 수집 데이터와 함께 동작하는지 확인한다. */
    @Test void mainPagedBookQueryWorksWithCatalogMetadata() {
        catalog.upsert(List.of(book(1, "소설", 1), book(2, "소설", 2), book(3, "인문", 3)));
        books.flush();
        var first = books.findByGenre_Name("소설", org.springframework.data.domain.PageRequest.of(0, 1));
        var second = books.findByGenre_Name("소설", org.springframework.data.domain.PageRequest.of(1, 1));
        assertEquals(1, first.size());
        assertEquals(1, second.size());
        assertNotEquals(first.getFirst().getId(), second.getFirst().getId());
        assertEquals("소설", first.getFirst().getGenre().getName());
    }
    @Test void controllersEnforceOwnershipValidationAndSyncToken() throws Exception {
        var user=user();
        var mvc=MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(get("/").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(user)))
                .apply(springSecurity()).build();
        try {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,"",user.getAuthorities()));
            mvc.perform(get("/api/recommend/books")).andExpect(status().isOk());
            mvc.perform(get("/api/recommend/books").param("userId","999999")).andExpect(status().isForbidden());
            mvc.perform(get("/api/recommend/books").param("limit","1000")).andExpect(status().isBadRequest());
            mvc.perform(get("/api/books/search").param("query"," ")).andExpect(status().isBadRequest());
            mvc.perform(get("/api/genres")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(13));
            mvc.perform(get("/api/books/search").param("query","소설")).andExpect(status().isServiceUnavailable());
            mvc.perform(post("/api/books/sync/popular").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isForbidden());
        } finally { SecurityContextHolder.clearContext(); }
    }
}
