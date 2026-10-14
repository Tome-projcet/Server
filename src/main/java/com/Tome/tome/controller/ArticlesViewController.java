package com.Tome.tome.controller;


import com.Tome.tome.domain.articles;
import com.Tome.tome.dto.ArticleViewResponses;
import com.Tome.tome.repository.articlesRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller

public class ArticlesViewController {

    private final articlesRepository articlesRepository;

    public ArticlesViewController(articlesRepository articlesRepository) {
        this.articlesRepository = articlesRepository;
    }

    @GetMapping("/articlesAll")
    public String viewAll(){
        return "articles";
    }

    @GetMapping("/article/{id}")
    public String viewarticle(@PathVariable Long id, Model model){
        articles articles = articlesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found" + id));

        model.addAttribute("article", articles);
        return "article";
    }

    @GetMapping("/new-article")
    public String createArticle(@RequestParam(required = false) Long id, Model model){
        if(id == null){
            model.addAttribute("article", new ArticleViewResponses());
        }
        else{
            articles articles = articlesRepository.findById(id).orElseThrow();
            model.addAttribute("article", new ArticleViewResponses(articles));
        }
        return "newarticle";
    }
}