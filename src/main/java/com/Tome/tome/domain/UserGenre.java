package com.Tome.tome.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class UserGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "count")
    private int count;


    @Builder
    public UserGenre(String name, User user) {
        this.name = name;
        this.count = 0;
        this.user = user;
    }

    public void increaseCount(){
        this.count += 1;
    }
}