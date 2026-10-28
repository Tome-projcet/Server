package com.Tome.tome.domain.genre.repository;

import com.Tome.tome.domain.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 공통 장르 이름으로 기존 행을 찾아 ID를 재사용한다. */
public interface GenreRepository extends JpaRepository<Genre, Long> {

    /** 공통 장르 이름이 일치하는 행을 조회한다. */
    Optional<Genre> findByName(String name);
}
