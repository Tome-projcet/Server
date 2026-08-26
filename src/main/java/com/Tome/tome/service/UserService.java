package com.Tome.tome.service;


import com.Tome.tome.domain.User;
import com.Tome.tome.dto.AddUserRequest;
import com.Tome.tome.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public Long save(AddUserRequest dto) throws IllegalArgumentException {
        if (dto.getPassword().length() < 8) {
            throw new IllegalArgumentException("조건이 틀렸습니다");
        }

        Optional<User> result = userRepository.findByEmail(dto.getEmail());
        if (result.isPresent()) {
            throw new IllegalArgumentException("이메일 존재");
        } else {
            result = userRepository.findByNickname(dto.getNickname());
            if (result.isPresent()) {
                throw new IllegalArgumentException("닉네임 존재");
            } else {
                result = userRepository.findByUsername(dto.getUsername());
                if (result.isPresent()) {
                    throw new IllegalArgumentException("아이디 존재");
                } else {
                    return userRepository.save(User.builder().email(dto.getEmail()).nickname(dto.getNickname()).password(passwordEncoder.encode(dto.getPassword())).username(dto.getUsername()).build()).getId();
                }
            }
        }
    }
}