package com.Tome.tome.domain.article.dto.reponse;

import com.Tome.tome.domain.article.entity.ArticleGenre;

public record ArticlesGenreResponse(
    String name
) {

    public static ArticlesGenreResponse from(ArticleGenre articleGenre){
        return new ArticlesGenreResponse(
                articleGenre.getBookname()
        );
    }
}
