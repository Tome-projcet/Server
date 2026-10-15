package com.Tome.tome.dto;

import com.Tome.tome.domain.Book;
import lombok.Getter;

@Getter

public class BookResponseDto {
    private Long id;
    private String isbn13;
    private String title;
    private String writer;
    private String bookurl;
    private String bookbuyurl;
    private String publisher;
    private String genre;
    private String description;
    private String source;
    private long loanCount;
    private String recommendationReason;

    public BookResponseDto(Book book) {
        this.id = book.getId();
        this.isbn13 = book.getIsbn13();
        this.title = book.getTitle();
        this.writer = book.getWriter();
        this.bookurl = book.getBookurl();
        this.bookbuyurl = book.getBookbuyurl();
        this.publisher = book.getPublisher();
        this.genre = book.getGenre() == null ? "기타" : book.getGenre().getName();
        this.description = book.getDescription();
        this.source = book.getSource();
        this.loanCount = book.getLoanCount() == null ? 0 : book.getLoanCount();
    }
    public BookResponseDto(Book book, String reason) {
        this(book);
        this.recommendationReason = reason;
    }
}
