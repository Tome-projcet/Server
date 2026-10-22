package com.Tome.tome.domain.genre.repository;

import com.Tome.tome.domain.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
