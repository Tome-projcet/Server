package com.Tome.tome.domain.article.controller;

import com.Tome.tome.domain.article.dto.request.AddArticleRequest;
import com.Tome.tome.domain.article.dto.reponse.ArticleViewResponses;
import com.Tome.tome.domain.article.dto.request.UpdateArticleRequest;
import com.Tome.tome.domain.article.dto.reponse.articleResponses;
import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.article.port.in.ArticleUseCase;
import com.Tome.tome.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class ArticlesApiController {
    private final ArticleUseCase articleUseCase;

    @PostMapping("/api/articles")
    public ResponseEntity<ArticleViewResponses> addArticles(@RequestBody AddArticleRequest request,
                                                            @AuthenticationPrincipal User user){
        Articles saveData = articleUseCase.save(request, user);

        return ResponseEntity.status(201).body(new ArticleViewResponses(saveData));
    }

    @GetMapping("/api/articles")
    public ResponseEntity<List<articleResponses>> allArticles(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size){
        List<articleResponses> find_articles = articleUseCase.findAll(page, size).stream().map(articleResponses::new).toList();

        return ResponseEntity.status(200).body(find_articles);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleViewResponses> findArticles(@PathVariable Long id){
        Articles articles1 = articleUseCase.findById(id);

        return ResponseEntity.status(200).body(new ArticleViewResponses(articles1));
    }

    @PutMapping("/api/articles/{id}/good/up")
    public ResponseEntity<Void> upgood(@PathVariable Long id,
                                       @AuthenticationPrincipal User user){
        articleUseCase.upgood(id, user);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticles(@PathVariable Long id,
                                               @AuthenticationPrincipal User user){
        if(articleUseCase.delete(id, user)){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(404).build();
    }

    @PutMapping("/api/articles/{id}")
    public ResponseEntity<ArticleViewResponses> updateArticles(@PathVariable Long id, @RequestBody UpdateArticleRequest request,
                                                               @AuthenticationPrincipal User user){
        return articleUseCase.update(id, request, user)
                .map(updatearticles -> ResponseEntity.status(200).body(new ArticleViewResponses(updatearticles)))
                .orElseGet(() -> ResponseEntity.status(404).build());
    }
}
