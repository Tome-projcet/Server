package com.Tome.tome.domain.article.entity;

import com.Tome.tome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;

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

    @Column(name = "bookreport")
    private String bookreport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @Builder
    public Articles(String bookreport, User user){
        this.bookreport = bookreport;
        this.good = 0;
        this.user = user;
    }

    public void update(String bookreport){
        this.bookreport = bookreport;
    }

    public void upgood(){
        this.good += 1;
    }
}