package com.Tome.tome.domain;


import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Table(name = "articles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class articles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "good")
    private int good;

    @Column(name = "time")
    private Date time;

    @Column(name = "bookreport")
    private String bookreport;


    @Builder
    public articles(String bookreport, Date time, int good){
        this.bookreport = bookreport;
        this.time = time;
        this.good = good;
    }

    public void update(String bookreport, int good){
        this.bookreport = bookreport;
        this.good = good;
    }
}