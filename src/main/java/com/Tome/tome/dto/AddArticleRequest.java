package com.Tome.tome.dto;


import com.Tome.tome.domain.articles;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddArticleRequest {
    private int good;
    private Date time;
    private String bookreport;

    public articles toEntity(){
        return articles.builder().good(good).time(time).bookreport(bookreport).build();
    }
}
