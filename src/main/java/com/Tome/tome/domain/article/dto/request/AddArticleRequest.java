package com.Tome.tome.domain.article.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddArticleRequest {
    private String bookreport;
    private String isbn;
}
