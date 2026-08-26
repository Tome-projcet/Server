package com.Tome.tome.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class AddUserRequest {
    private String email;
    private String password;
    private String username;
    private String nickname;

}
