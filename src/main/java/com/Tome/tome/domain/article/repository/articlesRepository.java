package com.Tome.tome.domain.article.repository;

import com.Tome.tome.domain.article.entity.Articles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;

public interface articlesRepository extends JpaRepository<Articles, Long> {
    Optional<Articles> findByBookreport(String bookreport);
    Optional<Articles> findByTime(Date time);
    Optional<Articles> findByGood(int good);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Articles a set a.good = a.good + 1 where a.id = :id")
    int increaseGood(@Param("id") Long id);
}
