package com.Tome.tome.domain.genre.repository;

import com.Tome.tome.domain.book.entity.Book;
import com.Tome.tome.domain.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface GenreRepository extends JpaRepository<Genre, Long> {

    List<Genre> findByBook(Book book);
}
