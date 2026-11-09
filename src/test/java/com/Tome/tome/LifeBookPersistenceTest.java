package com.Tome.tome;

import com.Tome.tome.domain.book.infra.ExternalBook;
import com.Tome.tome.domain.book.repository.BookRepository;
import com.Tome.tome.domain.book.service.*;
import com.Tome.tome.domain.user.dto.PageUserResponse;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.repository.UserRepository;
import com.Tome.tome.domain.user.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 테스트 자체의 트랜잭션 없이 커밋된 데이터·지연 로딩·동시 요청을 확인한다. */
@SpringBootTest(properties={"spring.jpa.open-in-view=false","spring.datasource.url=jdbc:h2:mem:life-persistence;MODE=PostgreSQL;NON_KEYWORDS=SECOND;DB_CLOSE_DELAY=-1"})
class LifeBookPersistenceTest {
    @Autowired UserRepository users;
    @Autowired BookRepository books;
    @Autowired LifeBookService service;
    @Autowired LifeBookStore store;
    @Autowired BookCatalogStore catalog;
    @MockitoBean BookCatalogService external;

    User user(){return users.save(User.builder().email(UUID.randomUUID()+"@example.org").nickname("persist").password("hash").build());}
    String isbn(int n) {
        String prefix="978000001"+String.format("%03d",n);
        int sum=0;for(int i=0;i<12;i++)sum+=(prefix.charAt(i)-'0')*(i%2==0?1:3);
        return prefix+(10-sum%10)%10;
    }
    ExternalBook book(String isbn){return new ExternalBook(isbn,"영구 저장","작가","","","출판사","소개","소설",1,"KAKAO");}

    @Test void committedSelectionLoadsWithoutOpenSessionAndInvalidReplacementRollsBack() {
        var user=user();String isbn=isbn(1);catalog.upsert(List.of(book(isbn)));
        var selected=service.set(user.getId(),isbn);
        // findById가 끝난 뒤에는 영속성 컨텍스트가 없으므로 EntityGraph의 동작도 확인한다.
        var profile=PageUserResponse.from(users.findById(user.getId()).orElseThrow());
        assertEquals(selected.getId(),profile.lifeBook().getId());assertEquals("소설",profile.lifeBook().getGenre());
        assertThrows(NoSuchElementException.class,()->store.set(user.getId(),Long.MAX_VALUE));
        assertEquals(selected.getId(),store.get(user.getId()).orElseThrow().getId());
        store.clear(user.getId());assertTrue(store.get(user.getId()).isEmpty());
        assertTrue(books.existsById(selected.getId()));
    }
    @Test void concurrentReplacementOfOneUsersSelectionIsSerialized() throws Exception {
        var user=user();String first=isbn(2),second=isbn(3);catalog.upsert(List.of(book(first),book(second)));
        var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
        try {
            var a=pool.submit(()->{start.await();return service.set(user.getId(),first);});
            var b=pool.submit(()->{start.await();return service.set(user.getId(),second);});
            start.countDown();assertNotNull(a.get(10,TimeUnit.SECONDS));assertNotNull(b.get(10,TimeUnit.SECONDS));
            assertTrue(Set.of(first,second).contains(store.get(user.getId()).orElseThrow().getIsbn13()));
        } finally {pool.shutdownNow();}
    }
    @Test void concurrentFirstSelectionOfTheSameNewBookDoesNotDuplicateOrFail() throws Exception {
        var pool=Executors.newFixedThreadPool(2);
        try {
            for(int round=10;round<18;round++) {
                String isbn=isbn(round);var first=user();var second=user();
                var bothLookup=new CountDownLatch(2);
                when(external.details(isbn)).thenAnswer(call->{bothLookup.countDown();assertTrue(bothLookup.await(5,TimeUnit.SECONDS));return book(isbn);});
                var a=pool.submit(()->service.set(first.getId(),isbn));
                var b=pool.submit(()->service.set(second.getId(),isbn));
                var one=a.get(10,TimeUnit.SECONDS);var two=b.get(10,TimeUnit.SECONDS);
                assertEquals(one.getId(),two.getId());
                assertEquals(one.getId(),store.get(first.getId()).orElseThrow().getId());
                assertEquals(one.getId(),store.get(second.getId()).orElseThrow().getId());
            }
        } finally {pool.shutdownNow();}
    }
}
