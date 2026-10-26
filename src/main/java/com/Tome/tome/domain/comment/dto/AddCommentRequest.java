package com.Tome.tome.domain.comment.dto;

import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddCommentRequest {

    private String comment;
    private int bad;

    public Comment toEntity(Articles articles){
        return Comment.builder().comment(comment).articles(articles).build();
    }
}
