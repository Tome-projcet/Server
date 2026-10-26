package com.Tome.tome.domain.comment.entity;

import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "Comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "good")
    private int good;

    @Column(name = "bad")
    private int bad;

    @CreatedDate
    @Column(name = "CreateAt")
    private LocalDateTime CreateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articles_id")
    private Articles articles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Comment(String comment, Articles articles,User user){
        this.comment = comment;
        this.articles = articles;
        this.user = user;
    }

    public String findComment(){
        return comment;
    }

    public int findGood(){
        return good;
    }

    public int findBad(){
        return bad;
    }

    public void update(String comment){
        this.comment = comment;
    }
}
