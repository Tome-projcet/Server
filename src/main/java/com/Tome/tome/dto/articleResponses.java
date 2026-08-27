package com.Tome.tome.dto;


import com.Tome.tome.domain.articles;
import jakarta.persistence.Column;
import lombok.Getter;

import java.util.Date;

@Getter
public class articleResponses {
    private int good;
    private Date time;
    private String bookreport;

    public articleResponses(articles articles){
        this.good = articles.getGood();
        this.time = articles.getTime();
        this.bookreport = articles.getBookreport();
    }
}
