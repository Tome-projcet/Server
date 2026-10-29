package com.Tome.tome.domain.article.entity;

import com.Tome.tome.domain.book.entity.Book;
import com.Tome.tome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "articles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Articles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "good")
    private int good;

    @Column(name = "bad")
    private int bad;

    @CreatedDate
    @Column(name = "time")
    private LocalDateTime time;

    @Column(name = "bookreport", columnDefinition = "TEXT")
    private String bookreport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;


    @Builder
    public Articles(String bookreport, User user, Book book){
        this.bookreport = bookreport;
        this.good = 0;
        this.user = user;
        this.book = book;
    }

    public void update(String bookreport){
        this.bookreport = bookreport;
    }

    public void upGood(){
        this.good += 1;
    }

    public void downGood(){
        this.good -=1;
    }
}