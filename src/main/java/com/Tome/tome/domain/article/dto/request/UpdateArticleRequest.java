package com.Tome.tome.domain.article.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateArticleRequest {
    private Date time;
    private String bookreport;
}
