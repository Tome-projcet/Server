package com.Tome.tome.domain.comment.service;

import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.article.repository.articlesRepository;
import com.Tome.tome.domain.comment.dto.AddCommentRequest;
import com.Tome.tome.domain.comment.dto.UpdateCommentRequest;
import com.Tome.tome.domain.comment.entity.Comment;
import com.Tome.tome.domain.comment.port.in.CommentUseCase;
import com.Tome.tome.domain.comment.repository.CommentRepository;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.global.exception.ForbiddenException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class CommentService implements CommentUseCase {
    private final CommentRepository commentRepository;
    private final articlesRepository articlesRepository;

    @Override
    public Comment save(Long articleId, AddCommentRequest request, User user){
        if(request.getComment() == null || request.getComment().isBlank()){
            throw new IllegalArgumentException("댓글 내용을 입력해주세요");
        }
        Articles articles = articlesRepository.findById(articleId).orElseThrow(() -> new NoSuchElementException("독후감을 찾을 수 없습니다"));
        Comment comment = Comment.builder()
                .comment(request.getComment()).articles(articles).user(user).build();
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> findByArticle(Long articleId){
        return commentRepository.findByArticles_Id(articleId);
    }

    @Override
    @Transactional
    public Comment update(Long id, UpdateCommentRequest request, User user){
       Comment comment = commentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다"));
       if(comment.getUser() == null || !user.getId().equals(comment.getUser().getId())){
           throw new ForbiddenException("작성자만 수정할 수 있습니다");
       }
       if(request.getComment() == null || request.getComment().isBlank()){
           throw new IllegalArgumentException("댓글 내용을 입력해주세요");
       }
       comment.update(request.getComment());

       return comment;
    }

    @Override
    @Transactional
    public boolean delete(Long id, User user){
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다"));
        if(comment.getUser() == null || !comment.getUser().getId().equals(user.getId())){
            throw new ForbiddenException("작성자만 삭제할 수 있습니다");
        }

        commentRepository.delete(comment);
        return true;
    }
}
