package com.Tome.tome.service;


import com.Tome.tome.domain.User;
import com.Tome.tome.dto.AddUserRequest;
import com.Tome.tome.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;



    public Long save(AddUserRequest dto) throws IllegalArgumentException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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
                return userRepository.save(User.builder().email(dto.getEmail()).nickname(dto.getNickname()).password(encoder.encode(dto.getPassword())).build()).getId();
            }
        }
    }

    public User findById(Long userId){
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("not found " + userId));
    }

    public User findByEmail(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("not found " + email));
        return user;
    }


    public void upfollwing(User targetuser,Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found " + id));

        targetuser.upFollowing();
        user.upFollow();
    }
}