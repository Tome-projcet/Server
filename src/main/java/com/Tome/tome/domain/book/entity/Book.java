package com.Tome.tome.domain.book.entity;

import com.Tome.tome.domain.genre.entity.Genre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** ISBN으로 식별되는 도서와 대표 장르를 저장한다. 추가 메타데이터는 기존 데이터에서 null일 수 있다. */
@Entity
@Getter
@NoArgsConstructor

public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String isbn13;

    @Column(nullable = false, length = 512)
    private String title;

    @Column(length = 1000)
    private String writer;

    @Column(length = 2048)
    private String bookurl;

    @Column(length = 2048)
    private String bookbuyurl;

    @Column(length = 512)
    private String publisher;

    @Column(length = 4000)
    private String description;

    private Long loanCount = 0L;
    private String source;
    private java.time.LocalDateTime catalogUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    private Genre genre;

    /** 기존 main 코드가 사용하는 도서 생성자 계약을 유지한다. */
    public Book(
            String isbn13,
            String title,
            String writer,
            String bookurl,
            String bookbuyurl,
            String publisher,
            Genre genre
    ) {
        this.isbn13 = isbn13;
        this.title = title;
        this.writer = writer;
        this.bookurl = bookurl;
        this.bookbuyurl = bookbuyurl;
        this.publisher = publisher;
        this.genre = genre;
    }
    /** 비어 있지 않은 메타데이터로 갱신하고 소개는 저장 한도에 맞춘다. 대출량·출처·갱신 시각도 기록한다. */
    public void updateCatalog(com.Tome.tome.domain.book.infra.ExternalBook book, Genre genre) {
        this.title = book.title();
        if (book.writer() != null && !book.writer().isBlank()) this.writer = book.writer();
        if (book.coverUrl() != null && !book.coverUrl().isBlank()) this.bookurl = book.coverUrl();
        if (book.detailUrl() != null && !book.detailUrl().isBlank()) this.bookbuyurl = book.detailUrl();
        if (book.publisher() != null && !book.publisher().isBlank()) this.publisher = book.publisher();
        if (book.description() != null && !book.description().isBlank())
            this.description = book.description().substring(0, Math.min(4000, book.description().length()));
        this.genre = genre;
        this.loanCount = Math.max(0, book.loanCount());
        this.source = book.source();
        this.catalogUpdatedAt = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);
    }
}
