package com.Tome.tome.domain.article.dto.reponse;

import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.book.entity.Book;
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
    private String bookurl;
    private String bookBuyUrl;
    private String title;
    private String writer;


    public ArticleViewResponses(Articles articles){
        Book book = articles.getBook();

        this.id = articles.getId();
        this.good = articles.getGood();
        this.bookreport = articles.getBookreport();
        this.time = articles.getTime();
        this.nickname = articles.getUser() != null ? articles.getUser().getNickname() : null;
        this.bookurl = book.getBookurl();
        this.bookBuyUrl = book.getBookbuyurl();
        this.title = book.getTitle();
        this.writer = book.getWriter();
    }
}
