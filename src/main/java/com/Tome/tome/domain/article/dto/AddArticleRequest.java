package com.Tome.tome.domain.article.dto;

import com.Tome.tome.domain.article.entity.Articles;
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
    private String bookreport;
}
