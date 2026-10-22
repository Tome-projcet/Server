package com.Tome.tome.domain.article.service;

import com.Tome.tome.domain.article.dto.AddArticleRequest;
import com.Tome.tome.domain.article.dto.UpdateArticleRequest;
import com.Tome.tome.domain.article.entity.ArticleGenre;
import com.Tome.tome.domain.article.entity.articles;
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


    public articles save(AddArticleRequest request){
        return articlesRepository.save(request.toEntity());
    }

    public List<articles> findAll(){
        return articlesRepository.findAll();
    }

    public articles findById(Long id){
        return articlesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("찾을 수 없음"));
    }

    public void delete(Long id){
        articlesRepository.deleteById(id);
    }


    @Transactional
    public void upgood(User user, articles articles){
        List<ArticleGenre> genreList = articleGenreRepository.findByArticles_Id(articles.getId());

        for(ArticleGenre item : genreList){
            Optional<UserGenre> userGenre = userGenreRepository.findByName(item.getBookname());
            userGenre.get().increaseCount();
        }
        articles.upgood();
    }

    @Transactional
    public articles Update(Long id, UpdateArticleRequest request){
        articles articles = articlesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found" + id));

        articles.update(request.getBookreport(), request.getGood());
        return articles;
    }
}
