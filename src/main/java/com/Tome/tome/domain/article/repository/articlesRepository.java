package com.Tome.tome.domain.article.repository;

import com.Tome.tome.domain.article.entity.Articles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;

public interface articlesRepository extends JpaRepository<Articles, Long> {

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Articles> findAll(Pageable pageable);

    Optional<Articles> findByBookreport(String bookreport);
    Optional<Articles> findByTime(Date time);
    Optional<Articles> findByGood(int good);

}
