package com.Tome.tome.repository;

import com.Tome.tome.domain.Book;
import com.Tome.tome.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn13(String isbn13);

    List<Book> findByGenre(Genre genre);
}
