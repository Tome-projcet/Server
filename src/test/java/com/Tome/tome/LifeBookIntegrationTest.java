package com.Tome.tome;

import com.Tome.tome.domain.book.infra.*;
import com.Tome.tome.domain.book.service.*;
import com.Tome.tome.domain.book.repository.BookRepository;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.repository.UserRepository;
import com.Tome.tome.domain.user.service.*;
import com.Tome.tome.domain.user.dto.PageUserResponse;
import com.Tome.tome.domain.article.service.articleService;
import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.article.repository.articlesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 서버가 조회한 도서를 인생 책·독후감에서 공유하고 기존 사용자 선택을 보존하는지 검증한다. */
@SpringBootTest(properties = "spring.jpa.open-in-view=false")
@Transactional
class LifeBookIntegrationTest {
 @MockitoBean BookCatalogService external;
 @Autowired LifeBookService service;
 @Autowired LifeBookStore store;
 @Autowired UserRepository users;
 @Autowired BookRepository books;
 @Autowired BookCatalogStore catalog;
 @Autowired articlesRepository articles;
 @Autowired articleService articleService;
 private User user() {return users.save(User.builder().email(UUID.randomUUID()+"@example.org").nickname(UUID.randomUUID().toString()).password("hash").build());}
 private ExternalBook record(String isbn,String genre) {return new ExternalBook(isbn,"선택한 책","작가","https://example.org/cover.jpg","https://example.org/book","출판사","소개",genre,10,"DATA4LIBRARY");}

 @Test void apiBookIsSavedOnceAndSharedWithOtherUsers() {
  var first=user();var second=user();
  var book=record("9788983921987","소설");
  when(external.details(book.isbn13())).thenReturn(book);
  var selected=service.set(first.getId(),"8983921986"); // 같은 책의 ISBN10
  var another=service.set(second.getId(),book.isbn13());
  assertEquals(selected.getId(),another.getId());assertEquals(1,books.count());
  assertEquals("소설",selected.getGenre());assertEquals("소개",selected.getDescription());
  verify(external,times(1)).details(book.isbn13());
  assertEquals(selected.getId(),PageUserResponse.from(users.findById(first.getId()).orElseThrow()).lifeBook().getId());
 }
 @Test void existingMultiGenreBookWorksWithoutExternalApi() {
  var user=user();var book=record("9788983921987","소설");catalog.upsert(List.of(book));
  var entity=books.findByIsbn13(book.isbn13()).orElseThrow();entity.addGenre("인문");books.flush();
  service.set(user.getId(),book.isbn13());
  assertEquals(2,entity.getGenres().size());assertEquals(entity.getId(),store.get(user.getId()).orElseThrow().getId());
  verifyNoInteractions(external);
 }
 @Test void failedLookupAndInvalidIsbnKeepExistingSelection() {
  var user=user();catalog.upsert(List.of(record("9788983921987","소설")));
  var selected=service.set(user.getId(),"9788983921987");
  assertThrows(IllegalArgumentException.class,()->service.set(user.getId(),"invalid"));
  when(external.details("9788983921994")).thenThrow(new BookApiException("도서 API 이용 불가"));
  assertThrows(BookApiException.class,()->service.set(user.getId(),"9788983921994"));
  assertEquals(selected.getId(),store.get(user.getId()).orElseThrow().getId());
 }
 @Test void replacementAndClearDoNotDeleteSharedBooksOrOtherSelections() {
  var first=user();var second=user();catalog.upsert(List.of(record("9788983921987","소설"),record("9788983921994","인문")));
  var original=service.set(first.getId(),"9788983921987");service.set(second.getId(),"9788983921987");
  var updated=service.set(first.getId(),"9788983921994");assertNotEquals(original.getId(),updated.getId());
  store.clear(first.getId());store.clear(first.getId());users.flush();
  assertTrue(store.get(first.getId()).isEmpty());
  assertEquals(original.getId(),store.get(second.getId()).orElseThrow().getId());assertEquals(2,books.count());
 }
 @Test void deletingAnArticlePreservesItsSharedLifeBookAndOtherArticles() {
  var user=user();catalog.upsert(List.of(record("9788983921987","소설")));
  var selected=service.set(user.getId(),"9788983921987");var book=books.findById(selected.getId()).orElseThrow();
  var first=articles.save(Articles.builder().user(user).book(book).bookreport("첫 독후감").build());
  var second=articles.save(Articles.builder().user(user).book(book).bookreport("둘째 독후감").build());
  articleService.delete(first.getId(),user);articles.flush();books.flush();
  assertTrue(articles.existsById(second.getId()));assertTrue(books.existsById(book.getId()));
  assertEquals(book.getId(),store.get(user.getId()).orElseThrow().getId());
 }
 @Test void missingApiBookDoesNotSetLifeBook() {
  var user=user();when(external.details("9788983921987")).thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"책 없음"));
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.set(user.getId(),"9788983921987"));
  assertTrue(store.get(user.getId()).isEmpty());assertEquals(0,books.count());
 }
 @Test void articleAndLifeBookSelectionReuseTheSameApiBook() {
  var user=user();var book=record("9788983921987","소설");
  when(external.details(book.isbn13())).thenReturn(book);
  var article=articleService.save(new com.Tome.tome.domain.article.dto.request.AddArticleRequest("독후감",book.isbn13()),user);
  var selected=service.set(user.getId(),book.isbn13());
  articles.flush();
  assertEquals(article.getBook().getId(),selected.getId());assertEquals(1,books.count());
  verify(external,times(1)).details(book.isbn13());
 }

}
