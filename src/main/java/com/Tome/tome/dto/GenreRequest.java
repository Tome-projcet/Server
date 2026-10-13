package com.Tome.tome.dto;

import com.Tome.tome.domain.User;
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
