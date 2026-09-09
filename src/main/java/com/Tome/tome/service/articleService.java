package com.Tome.tome.service;



import com.Tome.tome.domain.ArticleGenre;
import com.Tome.tome.domain.UserGenre;
import com.Tome.tome.domain.articles;
import com.Tome.tome.dto.AddArticleRequest;
import com.Tome.tome.dto.UpdateArticleRequest;
import com.Tome.tome.repository.ArticleGenreRepository;
import com.Tome.tome.repository.GenreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.Update;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import com.Tome.tome.repository.articlesRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class articleService {
    private final articlesRepository articlesRepository;
    private final GenreRepository genreRepository;
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
            Optional<UserGenre> userGenre = genreRepository.findByName(item.getBookname());
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
