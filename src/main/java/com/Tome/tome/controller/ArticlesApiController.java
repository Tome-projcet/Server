package com.Tome.tome.controller;


import com.Tome.tome.domain.articles;
import com.Tome.tome.dto.AddArticleRequest;
import com.Tome.tome.dto.UpdateArticleRequest;
import com.Tome.tome.dto.articleResponses;
import com.Tome.tome.repository.CommentRepository;
import com.Tome.tome.repository.articlesRepository;
import com.Tome.tome.service.CommentService;
import com.Tome.tome.service.articleService;
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



    @PostMapping("/api/articles")
    public ResponseEntity<articles> addArticles(@RequestBody AddArticleRequest request){
        articles saveData = articlesRepository.save(request.toEntity());

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