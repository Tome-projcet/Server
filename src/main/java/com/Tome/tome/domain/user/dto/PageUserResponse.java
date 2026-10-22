package com.Tome.tome.domain.user.dto;


import com.Tome.tome.domain.user.entity.User;
import jakarta.persistence.Column;

public record PageUserResponse(
        String nickname,
        String lifewriter,
        String bookcount,
        int follow,
        int follwing,
        String profileUrl
) {

    public static PageUserResponse from(User user){
        return new PageUserResponse(
                user.getNickname(),
                user.getLifewriter(),
                user.getBookcount(),
                user.getFollow(),
                user.getFollwing(),
                user.getProfileUrl()
        );
    }
}
