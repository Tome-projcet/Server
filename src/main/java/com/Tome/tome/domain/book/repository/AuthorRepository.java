package com.Tome.tome.domain.book.repository;

import com.Tome.tome.domain.book.entity.Author;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByNameIn(Collection<String> names);
    List<Author> findByNameContainingOrderByNameAsc(String keyword, Pageable pageable);
}
