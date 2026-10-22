package com.Tome.tome.domain.article.controller;

import com.Tome.tome.domain.article.dto.AddArticleRequest;
import com.Tome.tome.domain.article.dto.ArticleViewResponses;
import com.Tome.tome.domain.article.dto.UpdateArticleRequest;
import com.Tome.tome.domain.article.dto.articleResponses;
import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.article.repository.ArticleGenreRepository;
import com.Tome.tome.domain.article.repository.articlesRepository;
import com.Tome.tome.domain.article.service.articleService;
import com.Tome.tome.domain.comment.repository.CommentRepository;
import com.Tome.tome.domain.comment.service.CommentService;
import com.Tome.tome.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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
    public ResponseEntity<ArticleViewResponses> addArticles(@RequestBody AddArticleRequest request,
                                                @AuthenticationPrincipal User user  ){
        Articles saveData = articleService.save(request, user);

        return ResponseEntity.status(201).body(new ArticleViewResponses(saveData));
    }

    @GetMapping("/api/articles")
    public ResponseEntity<List<articleResponses>> allArticles(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size){
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "id"));
        List<articleResponses> find_articles = articlesRepository.findAll(pageRequest).stream().map(articleResponses::new).toList();

        return ResponseEntity.status(200).body(find_articles);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleViewResponses> findArticles(@PathVariable Long id){
        Articles articles1 = articlesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found" + id));

        return ResponseEntity.status(200).body(new ArticleViewResponses(articles1));
    }

    @PutMapping("/api/articles/{id}/good/up")
    public ResponseEntity<Void> upgood(@PathVariable Long id){
        articleService.upgood(id);

        return ResponseEntity.ok().build();
    }

    @Transactional
    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticles(@PathVariable Long id,
                               @AuthenticationPrincipal User user){
        Articles articles = articlesRepository.findById(id).orElseThrow();
        if(articles.getUser().getId().equals(user.getId())){
            commentRepository.deleteByArticles(articles);
            articleGenreRepository.deleteByArticles(articles);
            articlesRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(404).build();
    }


    @Transactional
    @PutMapping("/api/articles/{id}")
    public ResponseEntity<ArticleViewResponses> updateArticles(@PathVariable Long id, @RequestBody UpdateArticleRequest request,
                                                   @AuthenticationPrincipal User user){
        Articles articles = articlesRepository.findById(id).orElseThrow();
        if(articles.getUser().getId().equals(user.getId())){
            Articles updatearticles = articleService.Update(id, request);
            return ResponseEntity.status(200).body(new ArticleViewResponses(updatearticles));
        } else {
            return ResponseEntity.status(404).build();
        }
    }
}