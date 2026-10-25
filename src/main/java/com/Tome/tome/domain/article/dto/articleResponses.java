package com.Tome.tome.domain.article.dto;

import com.Tome.tome.domain.article.entity.Articles;
import jakarta.persistence.Column;
import lombok.Getter;

import java.util.Date;


@Getter
public class articleResponses {
    private int good;
    private String bookreport;
    private Long id;
    private String nickname;

    public articleResponses(Articles articles){
        this.good = articles.getGood();
        this.bookreport = truncate(articles.getBookreport(), 50);
        this.id = articles.getId();
        this.nickname = articles.getUser().getNickname();
    }

    private String truncate(String text, int maxLength){
        if(text == null || text.length() <= maxLength){
            return text;
        }

        return text.substring(0, maxLength) + "...";
    }
}
