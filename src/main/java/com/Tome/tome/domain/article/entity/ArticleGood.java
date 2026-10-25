package com.Tome.tome.domain.article.entity;

import com.Tome.tome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article_good", uniqueConstraints = @UniqueConstraint(columnNames = {"article_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ArticleGood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Articles articles;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public ArticleGood(Articles articles, User user){
        this.articles = articles;
        this.user = user;
    }
}
