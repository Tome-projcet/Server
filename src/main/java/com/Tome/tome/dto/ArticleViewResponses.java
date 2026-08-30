package com.Tome.tome.dto;


import com.Tome.tome.domain.articles;
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
