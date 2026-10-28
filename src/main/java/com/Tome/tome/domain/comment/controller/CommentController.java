package com.Tome.tome.domain.comment.controller;

import com.Tome.tome.domain.comment.dto.AddCommentRequest;
import com.Tome.tome.domain.comment.dto.CommentResponses;
import com.Tome.tome.domain.comment.dto.UpdateCommentRequest;
import com.Tome.tome.domain.comment.entity.Comment;
import com.Tome.tome.domain.comment.port.in.CommentUseCase;
import com.Tome.tome.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment", description = "댓글 API")
@RequiredArgsConstructor
@Controller
public class CommentController {
    private final CommentUseCase commentUseCase;

    @Operation(summary = "댓글 작성")
    @PostMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<CommentResponses> addComment(@PathVariable Long articleId, @RequestBody AddCommentRequest request,
                                                       @AuthenticationPrincipal User user){
        Comment saved = commentUseCase.save(articleId, request, user);
        return ResponseEntity.status(201).body(new CommentResponses(saved));
    }

    @Operation(summary = "댓글 목록 조회")
    @GetMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<List<CommentResponses>> getComments(@PathVariable Long articleId){
        List<CommentResponses> comment = commentUseCase.findByArticle(articleId).stream().map(CommentResponses::new).toList();
        return ResponseEntity.status(200).body(comment);
    }

    @Operation(summary = "댓글 삭제", description = "작성자가 아니면 403을 반환합니다.")
    @DeleteMapping("/api/articles/comments/{id}")
    public ResponseEntity<Void> deleteComments(@PathVariable Long id, @AuthenticationPrincipal User user){
        commentUseCase.delete(id, user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/api/articles/comments/{id}")
    public ResponseEntity<CommentResponses> updateComment(@PathVariable Long id, @AuthenticationPrincipal User user,
                                                 @RequestBody UpdateCommentRequest request){
            Comment comment = commentUseCase.update(id, request, user);
            return ResponseEntity.ok().body(new CommentResponses(comment));
    }
}
