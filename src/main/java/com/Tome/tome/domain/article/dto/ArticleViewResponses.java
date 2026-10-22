package com.Tome.tome.domain.article.dto;

import com.Tome.tome.domain.article.entity.articles;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Getter
public class ArticleViewResponses {

    private int good;
    private String bookreport;


    public ArticleViewResponses(articles articles){
        this.good = articles.getGood();
        this.bookreport = articles.getBookreport();
    }
}
