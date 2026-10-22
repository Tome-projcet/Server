package com.Tome.tome.domain.comment.controller;

import com.Tome.tome.domain.comment.dto.AddCommentRequest;
import com.Tome.tome.domain.comment.dto.CommentResponses;
import com.Tome.tome.domain.comment.entity.Comment;
import com.Tome.tome.domain.comment.repository.CommentRepository;
import com.Tome.tome.domain.comment.service.CommentService;
import com.Tome.tome.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class CommentController {
    private final CommentService commentService;
    private final CommentRepository commentRepository;

    @PostMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<CommentResponses> addComment(@PathVariable Long articleId, @RequestBody AddCommentRequest request,
                                              @AuthenticationPrincipal User user){
        Comment saved = commentService.save(articleId, request, user);
        return ResponseEntity.status(201).body(new CommentResponses(saved));
    }

    @GetMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<List<CommentResponses>> getComments(@PathVariable Long articleId){
        List<CommentResponses> comment = commentService.findByArticle(articleId).stream().map(CommentResponses::new).toList();
        return ResponseEntity.status(200).body(comment);
    }

    @DeleteMapping("/api/articles/comments/{id}")
    public ResponseEntity<Void> deleteComments(@PathVariable Long id, @AuthenticationPrincipal User user){
        Comment comment = commentRepository.findById(id).orElseThrow();
        if(user.getId().equals(comment.getUser().getId())){
            commentService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(404).build();
    }
}
