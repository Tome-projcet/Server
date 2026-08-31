package com.Tome.tome.domin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor

public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String isbn13;

    @Column(nullable = false)
    private String title;

    private String writer;

    private String bookurl;

    private String bookbuyurl;

    private String publisher;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;

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
}
