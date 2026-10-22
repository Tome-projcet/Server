package com.Tome.tome.domain.article.controller;

import com.Tome.tome.domain.article.dto.AddArticleRequest;
import com.Tome.tome.domain.article.dto.UpdateArticleRequest;
import com.Tome.tome.domain.article.dto.articleResponses;
import com.Tome.tome.domain.article.entity.articles;
import com.Tome.tome.domain.article.repository.ArticleGenreRepository;
import com.Tome.tome.domain.article.repository.articlesRepository;
import com.Tome.tome.domain.article.service.articleService;
import com.Tome.tome.domain.comment.repository.CommentRepository;
import com.Tome.tome.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RequiredArgsConstructor
@Controller
public class ArticlesApiController {
    private final articleService articleService;
    private final articlesRepository articlesRepository;
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final ArticleGenreRepository articleGenreRepository;



    @PostMapping("/api/articles")
    public ResponseEntity<articles> addArticles(@RequestBody AddArticleRequest request){
        articles saveData = articleService.save(request);

        return ResponseEntity.status(201).body(saveData);
    }

    @GetMapping("/api/articles")
    public ResponseEntity<List<articleResponses>> allArticles(){
        List<articleResponses> find_articles = articlesRepository.findAll().stream().map(articleResponses::new).toList();

        return ResponseEntity.status(200).body(find_articles);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<articles> findArticles(@PathVariable Long id){
        articles articles1 = articlesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found" + id));

        return ResponseEntity.status(200).body(articles1);
    }

    @GetMapping("/api/articles/{id}/good/up")
    public void upgood(@PathVariable Long id){
        articles articles = articlesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found " + id));

        articles.upgood();
    }

    @DeleteMapping("/api/articles/{id}")
    public void deleteArticles(@PathVariable Long id){
        articlesRepository.deleteById(id);
    }


    @PutMapping("/api/articles/{id}")
    public ResponseEntity<articles> updateArticles(@PathVariable Long id, @RequestBody UpdateArticleRequest request){
        articles updatearticles = articleService.Update(id, request);

        return ResponseEntity.status(200).body(updatearticles);
    }
}