package com.Tome.tome.domain.article.service;

import com.Tome.tome.domain.article.dto.AddArticleRequest;
import com.Tome.tome.domain.article.dto.UpdateArticleRequest;
import com.Tome.tome.domain.article.entity.ArticleGenre;
import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.article.repository.ArticleGenreRepository;
import com.Tome.tome.domain.article.repository.articlesRepository;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.entity.UserGenre;
import com.Tome.tome.domain.user.repository.UserGenreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class articleService {
    private final articlesRepository articlesRepository;
    private final UserGenreRepository userGenreRepository;
    private final ArticleGenreRepository articleGenreRepository;


    public Articles save(AddArticleRequest request, User user){
        Articles article = Articles.builder()
                .bookreport(request.getBookreport()).user(user).build();
        return articlesRepository.save(article);
    }

    public List<Articles> findAll(){
        return articlesRepository.findAll();
    }

    public Articles findById(Long id){
        return articlesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("찾을 수 없음"));
    }

    public void delete(Long id){
        articlesRepository.deleteById(id);
    }



    @Transactional
    public void upgood(Long id){
        if(articlesRepository.increaseGood(id) == 0){
            throw new IllegalArgumentException("not found " + id);
        }
    }

    @Transactional
    public Articles Update(Long id, UpdateArticleRequest request){
        Articles articles = articlesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found" + id));

        articles.update(request.getBookreport());
        return articles;
    }
}
