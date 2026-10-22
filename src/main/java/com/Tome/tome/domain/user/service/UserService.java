package com.Tome.tome.domain.user.service;

import com.Tome.tome.domain.user.dto.AddUserRequest;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.entity.UserGenre;
import com.Tome.tome.domain.user.repository.UserGenreRepository;
import com.Tome.tome.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserGenreRepository userGenreRepository;



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

    @Transactional
    public void upfollwing(User targetuser, Long id){
        User loginUser = findById(targetuser.getId());
        User user = findById(id);

        loginUser.upFollowing();
        user.upFollow();
    }

    @Transactional
    public User updateNickname(Long userId, String nickname){
        return findById(userId).update(nickname);
    }

    @Transactional
    public User updateWriter(Long userId, String writer){
        return findById(userId).updateWriter(writer);
    }

    @Transactional
    public User updateProfileUrl(Long userId, String url){
        return findById(userId).UpdateProfileUrl(url);
    }

    @Transactional
    public void checkGenre(User user, String genreName){
        Optional<UserGenre> existing = userGenreRepository.findByUserAndName(user, genreName);

        if (existing.isPresent()) {
            existing.get().increaseCount();
        } else {
            userGenreRepository.save(UserGenre.builder().name(genreName).user(user).build());
        }
    }
}
