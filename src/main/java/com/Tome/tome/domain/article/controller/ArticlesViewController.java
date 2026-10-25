package com.Tome.tome.domain.article.controller;

import com.Tome.tome.domain.article.dto.reponse.ArticleViewResponses;
import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.article.port.in.ArticleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class ArticlesViewController {

    private final ArticleUseCase articleUseCase;

    @GetMapping("/articlesAll")
    public String viewAll(){
        return "articles";
    }

    @GetMapping("/article/{id}")
    public String viewarticle(@PathVariable Long id, Model model){
        Articles articles = articleUseCase.findById(id);

        model.addAttribute("article", new ArticleViewResponses(articles));
        return "article";
    }

    @GetMapping("/new-article")
    public String createArticle(@RequestParam(required = false) Long id, Model model){
        if(id == null){
            model.addAttribute("article", new ArticleViewResponses());
        }
        else{
            Articles articles = articleUseCase.findById(id);
            model.addAttribute("article", new ArticleViewResponses(articles));
        }
        return "newarticle";
    }
}
