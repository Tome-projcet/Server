package com.Tome.tome.domain.article.port.in;

import com.Tome.tome.domain.article.dto.request.AddArticleRequest;
import com.Tome.tome.domain.article.dto.request.UpdateArticleRequest;
import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface ArticleUseCase {
    Articles save(AddArticleRequest request, User user);

    List<Articles> findAll(int page, int size);

    Articles findById(Long id);

    void upgood(Long id, User user);

    boolean delete(Long id, User user);

    Optional<Articles> update(Long id, UpdateArticleRequest request, User user);
}
