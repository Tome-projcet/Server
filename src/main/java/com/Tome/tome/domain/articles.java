package com.Tome.tome.domain;


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
public class articles {

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


    @Builder
    public articles(String bookreport, int good){
        this.bookreport = bookreport;
        this.good = good;
    }

    public void update(String bookreport, int good){
        this.bookreport = bookreport;
        this.good = good;
    }
}