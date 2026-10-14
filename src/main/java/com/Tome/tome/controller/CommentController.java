package com.Tome.tome.controller;


import com.Tome.tome.domain.Comment;
import com.Tome.tome.dto.AddCommentRequest;
import com.Tome.tome.dto.CommentResponses;
import com.Tome.tome.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long articleId, @RequestBody AddCommentRequest request){
        Comment saved = commentService.save(articleId, request);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<List<CommentResponses>> getComments(@PathVariable Long articleId){
        List<CommentResponses> comment = commentService.findByArticle(articleId).stream().map(CommentResponses::new).toList();
        return ResponseEntity.status(200).body(comment);
    }

    @DeleteMapping("/api/articles/comments/{id}")
    public void deleteComments(@PathVariable Long id){
        commentService.delete(id);
    }
}
