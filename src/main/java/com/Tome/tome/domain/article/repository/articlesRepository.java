package com.Tome.tome.domain.article.repository;

import com.Tome.tome.domain.article.entity.articles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface articlesRepository extends JpaRepository<articles, Long> {
    Optional<articles> findByBookreport(String bookreport);
    Optional<articles> findByTime(Date time);
    Optional<articles> findByGood(int good);
}
