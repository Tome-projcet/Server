package com.Tome.tome.repository;


import com.Tome.tome.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByComment(String comment);
    Optional<Comment> findByGood(int good);
    Optional<Comment> findByBad(int bad);

    List<Comment> findByArticles_Id(Long articleId);
}
