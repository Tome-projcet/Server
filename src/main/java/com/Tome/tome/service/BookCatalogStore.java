package com.Tome.tome.service;

import com.Tome.tome.catalog.*;
import com.Tome.tome.domain.*;
import com.Tome.tome.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookCatalogStore {
    private final BookRepository books;
    private final GenreRepository genres;
    private final GenreTaxonomy taxonomy;
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Genre> listGenres() {
        return taxonomy.names().stream().map(genres::findByName).flatMap(Optional::stream).toList();
    }
    @Transactional
    public List<Genre> ensureGenres() {
        for (String name : taxonomy.names())
            if (genres.findByName(name).isEmpty()) genres.save(new Genre(name));
        return taxonomy.names().stream().map(name -> genres.findByName(name).orElseThrow()).toList();
    }
}
