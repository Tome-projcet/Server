package com.Tome.tome.domain.user.entity;

import com.Tome.tome.domain.article.entity.Articles;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_follow")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_user_id")
    private User mainUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    private UserFollow(User mainUser, User user){
        this.mainUser = mainUser;
        this.user = user;
    }
}
