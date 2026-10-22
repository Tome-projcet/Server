package com.Tome.tome.domain.article.dto;

import com.Tome.tome.domain.article.entity.Articles;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class ArticleViewResponses {

    private Long id;
    private int good;
    private String bookreport;
    private LocalDateTime time;
    private String nickname;


    public ArticleViewResponses(Articles articles){
        this.id = articles.getId();
        this.good = articles.getGood();
        this.bookreport = articles.getBookreport();
        this.time = articles.getTime();
        this.nickname = articles.getUser() != null ? articles.getUser().getNickname() : null;
    }
}
