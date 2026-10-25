package com.Tome.tome.domain.comment.port.in;

import com.Tome.tome.domain.comment.dto.AddCommentRequest;
import com.Tome.tome.domain.comment.dto.UpdateCommentRequest;
import com.Tome.tome.domain.comment.entity.Comment;
import com.Tome.tome.domain.user.entity.User;

import java.util.List;

public interface CommentUseCase {
    Comment save(Long articleId, AddCommentRequest request, User user);

    List<Comment> findByArticle(Long articleId);

    Comment update(Long id, UpdateCommentRequest request, User user);

    boolean delete(Long id, User user);
}
