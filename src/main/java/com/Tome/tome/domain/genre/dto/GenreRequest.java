package com.Tome.tome.domain.genre.dto;

import com.Tome.tome.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GenreRequest {
    private String name;
    private int count;
    private User user;
}
