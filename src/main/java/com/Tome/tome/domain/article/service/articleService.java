package com.Tome.tome.domain.article.service;

import com.Tome.tome.domain.article.dto.AddArticleRequest;
import com.Tome.tome.domain.article.dto.UpdateArticleRequest;
import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.article.port.in.ArticleUseCase;
import com.Tome.tome.domain.article.repository.ArticleGenreRepository;
import com.Tome.tome.domain.article.repository.articlesRepository;
import com.Tome.tome.domain.comment.repository.CommentRepository;
import com.Tome.tome.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class articleService implements ArticleUseCase {
    private static final int MAX_PAGE_SIZE = 100;

    private final articlesRepository articlesRepository;
    private final ArticleGenreRepository articleGenreRepository;
    private final CommentRepository commentRepository;

    @Override
    public Articles save(AddArticleRequest request, User user){
        Articles article = Articles.builder()
                .bookreport(request.getBookreport()).user(user).build();
        return articlesRepository.save(article);
    }

    @Override
    public List<Articles> findAll(int page, int size){
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "id"));
        return articlesRepository.findAll(pageRequest).getContent();
    }

    @Override
    public Articles findById(Long id){
        return articlesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found " + id));
    }

    @Override
    @Transactional
    public void upgood(Long id){
        if(articlesRepository.increaseGood(id) == 0){
            throw new IllegalArgumentException("not found " + id);
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id, User user){
        Articles articles = articlesRepository.findById(id).orElseThrow();
        if(!isWriter(articles, user)){
            return false;
        }

        commentRepository.deleteByArticles(articles);
        articleGenreRepository.deleteByArticles(articles);
        articlesRepository.delete(articles);
        return true;
    }

    @Override
    @Transactional
    public Optional<Articles> update(Long id, UpdateArticleRequest request, User user){
        Articles articles = articlesRepository.findById(id).orElseThrow();
        if(!isWriter(articles, user)){
            return Optional.empty();
        }

        articles.update(request.getBookreport());
        return Optional.of(articles);
    }

    private boolean isWriter(Articles articles, User user){
        return articles.getUser() != null && articles.getUser().getId().equals(user.getId());
    }
}
