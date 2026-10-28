package com.Tome.tome.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자와 관심 장르 이름·선호 카운트를 연결한다. 기존 main의 생성자와 필드를 유지한다. */
@Entity
@Getter
@NoArgsConstructor
public class UserGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "count")
    private int count;


    /** 기존 계약에 따라 초기 카운트는 0이다. 추천에서는 기존 0도 선택한 관심사로 본다. */
    @Builder
    public UserGenre(String name, User user) {
        this.name = name;
        this.count = 0;
        this.user = user;
    }

    /** 관심사 교체 시 기존 행과 카운트를 유지하면서 main의 이름 일치 조회에 사용할 표준 이름을 저장한다. */
    public void rename(String canonicalName) {
        this.name = canonicalName;
    }

    /** 기존 온보딩·활동 코드가 장르 선호도를 한 단계 증가시킬 때 사용한다. */
    public void increaseCount(){
        this.count += 1;
    }
}
