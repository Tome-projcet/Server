package com.Tome.tome.domain.article.controller;

import com.Tome.tome.domain.article.dto.request.AddArticleRequest;
import com.Tome.tome.domain.article.dto.reponse.ArticleViewResponses;
import com.Tome.tome.domain.article.dto.request.UpdateArticleRequest;
import com.Tome.tome.domain.article.dto.reponse.articleResponses;
import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.article.port.in.ArticleUseCase;
import com.Tome.tome.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Article", description = "독후감 API")
@RequiredArgsConstructor
@Controller
public class ArticlesApiController {
    private final ArticleUseCase articleUseCase;

    @Operation(summary = "독후감 작성")
    @PostMapping("/api/articles")
    public ResponseEntity<ArticleViewResponses> addArticles(@RequestBody AddArticleRequest request,
                                                            @AuthenticationPrincipal User user){
        Articles saveData = articleUseCase.save(request, user);

        return ResponseEntity.status(201).body(new ArticleViewResponses(saveData));
    }

    @Operation(summary = "독후감 목록 조회", description = "page는 0부터, size는 최대 100입니다. 최신 글부터 반환합니다.")
    @GetMapping("/api/articles")
    public ResponseEntity<List<articleResponses>> allArticles(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size){
        List<articleResponses> find_articles = articleUseCase.findAll(page, size).stream().map(articleResponses::new).toList();

        return ResponseEntity.status(200).body(find_articles);
    }

    @Operation(summary = "독후감 상세 조회")
    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleViewResponses> findArticles(@PathVariable Long id){
        Articles articles1 = articleUseCase.findById(id);

        return ResponseEntity.status(200).body(new ArticleViewResponses(articles1));
    }

    @Operation(summary = "좋아요 / 좋아요 취소")
    @PutMapping("/api/articles/{id}/good/up")
    public ResponseEntity<Void> upgood(@PathVariable Long id,
                                       @AuthenticationPrincipal User user){
        articleUseCase.upgood(id, user);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "독후감 삭제", description = "작성자가 아니면 403을 반환합니다.")
    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticles(@PathVariable Long id,
                                               @AuthenticationPrincipal User user){
        articleUseCase.delete(id, user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "독후감 수정", description = "작성자가 아니면 403을 반환합니다.")
    @PutMapping("/api/articles/{id}")
    public ResponseEntity<ArticleViewResponses> updateArticles(@PathVariable Long id, @RequestBody UpdateArticleRequest request,
                                                               @AuthenticationPrincipal User user){
        Articles updatearticles = articleUseCase.update(id, request, user).orElseThrow();
        return ResponseEntity.status(200).body(new ArticleViewResponses(updatearticles));
    }
}
