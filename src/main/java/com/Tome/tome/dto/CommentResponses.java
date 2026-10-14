package com.Tome.tome.dto;


import com.Tome.tome.domain.Comment;

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
