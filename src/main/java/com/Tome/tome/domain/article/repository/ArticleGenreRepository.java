package com.Tome.tome.domain.article.repository;

import com.Tome.tome.domain.article.entity.ArticleGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleGenreRepository extends JpaRepository<ArticleGenre, Long> {
    List<ArticleGenre> findByArticles_Id(Long article_id);
}
