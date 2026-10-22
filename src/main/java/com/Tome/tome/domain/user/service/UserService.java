package com.Tome.tome.domain.user.service;

import com.Tome.tome.domain.user.dto.AddUserRequest;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.entity.UserFollow;
import com.Tome.tome.domain.user.entity.UserGenre;
import com.Tome.tome.domain.user.repository.UserFollowRepository;
import com.Tome.tome.domain.user.repository.UserGenreRepository;
import com.Tome.tome.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserGenreRepository userGenreRepository;
    private final UserFollowRepository userFollowRepository;
    private final PasswordEncoder passwordEncoder;


    public Long save(AddUserRequest dto) throws IllegalArgumentException {
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
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
                return userRepository.save(User.builder().email(dto.getEmail()).nickname(dto.getNickname()).password(passwordEncoder.encode(dto.getPassword())).build()).getId();
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
    public void upfollwing(User argetuser, Long id){
        User targetuser = userRepository.findById(argetuser.getId()).orElseThrow();
        User user = userRepository.findById(id).orElseThrow();
        if(userFollowRepository.findByUserAndMainUser(targetuser, user).isPresent()){
            userFollowRepository.deleteByMainUserAndUser(user, targetuser);
            userRepository.addFollowing(targetuser.getId(), -1);
            userRepository.addFollow(user.getId(), -1);
        } else {
            UserFollow userFollow = UserFollow.builder()
                    .mainUser(user).user(targetuser).build();
            userFollowRepository.save(userFollow);

            userRepository.addFollowing(targetuser.getId(), 1);
            userRepository.addFollow(user.getId(), 1);
        }
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
