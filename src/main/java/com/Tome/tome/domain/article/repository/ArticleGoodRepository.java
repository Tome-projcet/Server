package com.Tome.tome.domain.article.repository;

import com.Tome.tome.domain.article.entity.ArticleGood;
import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleGoodRepository extends JpaRepository<ArticleGood, Long> {
    Optional<ArticleGood> findByUserAndArticles(User user, Articles articles);

    void deleteByArticlesAndUser(Articles articles, User user);
}
