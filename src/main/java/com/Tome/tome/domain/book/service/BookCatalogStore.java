package com.Tome.tome.domain.book.service;

import com.Tome.tome.domain.book.infra.*;
import com.Tome.tome.domain.book.entity.Book;
import com.Tome.tome.domain.genre.entity.Genre;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.entity.UserGenre;
import com.Tome.tome.domain.book.repository.BookRepository;
import com.Tome.tome.domain.genre.repository.GenreRepository;
import com.Tome.tome.domain.user.repository.UserRepository;
import com.Tome.tome.domain.user.repository.UserGenreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

/** 도서와 장르의 DB 저장 경계를 담당한다. ISBN 유일 조건을 기준으로 신규 저장과 기존 정보 갱신을 구분한다. */
@Service
@RequiredArgsConstructor
public class BookCatalogStore {
    private final BookRepository books;
    private final GenreRepository genres;
    private final GenreTaxonomy taxonomy;
    /** 한 배치를 트랜잭션으로 저장한다. 외부 결과가 비어 있거나 ISBN이 잘못된 행은 저장하지 않는다. */
    @Transactional
    public synchronized int upsert(List<ExternalBook> records) {
        int saved = 0;
        for (ExternalBook record : records) {
            if (Isbn.normalize(record.isbn13()).isBlank() || record.title().isBlank()) continue;
            String name = taxonomy.canonical(record.genre());
            Genre genre = genres.findByName(name).orElseGet(() -> genres.save(new Genre(name)));
            Book book = books.findByIsbn13(record.isbn13()).orElseGet(() -> new Book(record.isbn13(),
                    record.title(), record.writer(), record.coverUrl(), record.detailUrl(), record.publisher(), genre));
            book.updateCatalog(record, genre);
            books.save(book);
            saved++;
        }
        return saved;
    }
    /** 정의된 공통 장르만 등록 순서대로 반환한다. 조회 과정에서 DB를 변경하지 않는다. */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Genre> listGenres() {
        return taxonomy.names().stream().map(genres::findByName).flatMap(Optional::stream).toList();
    }
    /** 부족한 공통 장르를 추가하며 기존 장르와 사용자 기록은 보존한다. */
    @Transactional
    public List<Genre> ensureGenres() {
        for (String name : taxonomy.names())
            if (genres.findByName(name).isEmpty()) genres.save(new Genre(name));
        return taxonomy.names().stream().map(name -> genres.findByName(name).orElseThrow()).toList();
    }
}
