package com.Tome.tome.dto;


import com.Tome.tome.domain.Comment;
import com.Tome.tome.domain.articles;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddCommentRequest {

    private String comment;
    private int good;
    private int bad;

    public Comment toEntity(articles articles){
        return Comment.builder().bad(bad).good(good).comment(comment).articles(articles).build();
    }
}
