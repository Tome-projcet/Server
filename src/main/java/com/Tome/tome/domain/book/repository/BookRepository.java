package com.Tome.tome.domain.book.repository;

import com.Tome.tome.domain.book.entity.Book;
import com.Tome.tome.domain.genre.entity.Genre;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn13(String isbn13);

    List<Book> findByGenre(Genre genre);

    List<Book> findByGenre_Name(String name);

    @EntityGraph(attributePaths = "genre")
    List<Book> findByGenre_Name(String name, Pageable pageable);
}