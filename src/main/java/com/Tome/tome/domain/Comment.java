package com.Tome.tome.domain;


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

    @Column(name = "comment")
    private String comment;

    @Column(name = "good")
    private int good;

    @Column(name = "bad")
    private int bad;

    @CreatedDate
    @Column(name = "Create At")
    private LocalDateTime CreateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articles_id")
    private articles articles;

    @Builder
    public Comment(String comment, int good, int bad, articles articles){
        this.comment = comment;
        this.good = good;
        this.bad = bad;
        this.articles = articles;
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

    public void update(String comment, int good, int bad){
        this.comment = comment;
        this.good = good;
        this.bad = bad;
    }
}
