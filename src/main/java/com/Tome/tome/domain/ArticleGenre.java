package com.Tome.tome.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "bookaricle")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ArticleGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String bookname;

    @OneToOne
    @JoinColumn(name = "articles_id")
    private articles articles;

    @Builder
    public ArticleGenre(String bookname, articles articles){
        this.articles = articles;
        this.bookname = bookname;
    }
}
