package com.Tome.tome.repository;

import com.Tome.tome.domain.Book;
import com.Tome.tome.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn13(String isbn13);

    List<Book> findByGenre(Genre genre);

    List<Book> findByGenre_Name(String name);
    @org.springframework.data.jpa.repository.Query("select b from Book b where b.genre.name in :names order by coalesce(b.loanCount, 0) desc, b.id asc")
    List<Book> findByGenre_NameIn(@org.springframework.data.repository.query.Param("names") java.util.Collection<String> names,
                                 org.springframework.data.domain.Pageable pageable);
    @org.springframework.data.jpa.repository.Query("select b from Book b order by coalesce(b.loanCount, 0) desc, b.id asc")
    List<Book> findAllBy(org.springframework.data.domain.Pageable pageable);
    List<Book> findByTitleContainingIgnoreCaseOrWriterContainingIgnoreCase(String title, String writer,
                                                                          org.springframework.data.domain.Pageable pageable);
}
