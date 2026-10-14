package com.Tome.tome.dto;


import com.Tome.tome.domain.articles;
import jakarta.persistence.Column;
import lombok.Getter;

import java.util.Date;


@Getter
public class articleResponses {
    private int good;
    private String bookreport;

    public articleResponses(articles articles){
        this.good = articles.getGood();
        this.bookreport = truncate(articles.getBookreport(), 50);
    }

    private String truncate(String text, int maxLength){
        if(text == null || text.length() <= maxLength){
            return text;
        }

        return text.substring(0, maxLength) + "...";
    }
}
