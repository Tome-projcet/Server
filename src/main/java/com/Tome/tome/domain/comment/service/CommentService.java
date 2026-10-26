package com.Tome.tome.domain.comment.service;

import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.article.repository.articlesRepository;
import com.Tome.tome.domain.comment.dto.AddCommentRequest;
import com.Tome.tome.domain.comment.dto.UpdateCommentRequest;
import com.Tome.tome.domain.comment.entity.Comment;
import com.Tome.tome.domain.comment.port.in.CommentUseCase;
import com.Tome.tome.domain.comment.repository.CommentRepository;
import com.Tome.tome.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService implements CommentUseCase {
    private final CommentRepository commentRepository;
    private final articlesRepository articlesRepository;

    @Override
    public Comment save(Long articleId, AddCommentRequest request, User user){
        Articles articles = articlesRepository.findById(articleId).orElseThrow(() -> new IllegalArgumentException("not found " + articleId));
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
       Comment comment = commentRepository.findById(id).orElseThrow(()->new IllegalArgumentException("not found " + id));
       if(!user.getId().equals(comment.getUser().getId())){
           throw new IllegalArgumentException("사용자가 일치하지 않습니다");
       }
       comment.update(request.getComment());

       return comment;
    }

    @Override
    @Transactional
    public boolean delete(Long id, User user){
        Comment comment = commentRepository.findById(id).orElseThrow();
        if(comment.getUser() == null || !comment.getUser().getId().equals(user.getId())){
            return false;
        }

        commentRepository.delete(comment);
        return true;
    }
}
