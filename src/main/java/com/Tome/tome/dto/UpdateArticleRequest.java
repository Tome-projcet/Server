package com.Tome.tome.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateArticleRequest {
    private int good;
    private Date time;
    private String bookreport;
}
