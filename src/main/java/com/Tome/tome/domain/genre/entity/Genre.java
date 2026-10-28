package com.Tome.tome.domain.genre.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 공통 장르 이름을 저장한다. 이름의 유일 조건으로 중복 장르 등록을 막는다. */
@Entity
@Getter
@NoArgsConstructor

public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    /** 표준화한 이름으로 장르를 생성한다. */
    public Genre(String name) {
        this.name = name;
    }
}
