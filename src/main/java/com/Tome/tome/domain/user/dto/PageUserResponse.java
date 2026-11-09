package com.Tome.tome.domain.user.dto;


import com.Tome.tome.domain.user.entity.User;

public record PageUserResponse(
        String nickname,
        String lifewriter,
        String bookcount,
        int follow,
        int follwing,
        String profileUrl,
        @io.swagger.v3.oas.annotations.media.Schema(description = "인생 책. 선택하지 않은 경우 null")
        com.Tome.tome.domain.book.dto.BookResponseDto lifeBook
) {

    public static PageUserResponse from(User user){
        return new PageUserResponse(
                user.getNickname(),
                user.getLifewriter(),
                user.getBookcount(),
                user.getFollow(),
                user.getFollwing(),
                user.getProfileUrl(),
                user.getLifeBook() == null ? null : new com.Tome.tome.domain.book.dto.BookResponseDto(user.getLifeBook())
        );
    }
}
