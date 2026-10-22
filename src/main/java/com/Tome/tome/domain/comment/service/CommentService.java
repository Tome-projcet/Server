package com.Tome.tome.domain.comment.service;

import com.Tome.tome.domain.article.entity.articles;
import com.Tome.tome.domain.article.repository.articlesRepository;
import com.Tome.tome.domain.comment.dto.AddCommentRequest;
import com.Tome.tome.domain.comment.dto.UpdateCommentRequest;
import com.Tome.tome.domain.comment.entity.Comment;
import com.Tome.tome.domain.comment.repository.CommentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final articlesRepository articlesRepository;

    public Comment save(Long articleId, AddCommentRequest request){
        articles articles = articlesRepository.findById(articleId).orElseThrow(() -> new IllegalArgumentException("not found " + articleId));
        return commentRepository.save(request.toEntity(articles));
    }

    public List<Comment> findByArticle(Long articleId){
        return commentRepository.findByArticles_Id(articleId);
    }

    @Transactional
    public Comment update(Long id, UpdateCommentRequest request){
       Comment comment = commentRepository.findById(id).orElseThrow(()->new IllegalArgumentException("not found " + id));
       comment.update(request.getComment(), request.getGood(), request.getBad());

       return comment;
    }

    public void delete(Long id){
        commentRepository.deleteById(id);
    }
}
