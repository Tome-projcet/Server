package com.Tome.tome.domain.comment.dto;

import com.Tome.tome.domain.comment.entity.Comment;
import lombok.Getter;

@Getter
public class CommentResponses {
    private String comment;
    private int good;
    private int bad;

    public CommentResponses(Comment comment){
        this.comment = comment.getComment();
        this.good = comment.getGood();
        this.bad = comment.getBad();
    }
}
