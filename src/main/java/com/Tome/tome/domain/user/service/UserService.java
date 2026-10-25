package com.Tome.tome.domain.user.service;

import com.Tome.tome.domain.user.dto.AddUserRequest;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.entity.UserFollow;
import com.Tome.tome.domain.user.entity.UserGenre;
import com.Tome.tome.domain.user.port.in.UserUseCase;
import com.Tome.tome.domain.user.repository.UserFollowRepository;
import com.Tome.tome.domain.user.repository.UserGenreRepository;
import com.Tome.tome.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService implements UserUseCase {
    private final UserRepository userRepository;
    private final UserGenreRepository userGenreRepository;
    private final UserFollowRepository userFollowRepository;
    private final PasswordEncoder passwordEncoder;

    private final static Pattern PROFILE_NAME = Pattern.compile("^http[A-Za-z]://[A-Za-z]*$");

    @Override
    public Long save(AddUserRequest dto) {
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

    @Override
    public User findById(Long userId){
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("not found " + userId));
    }

    @Override
    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("not found " + email));
    }

    @Override
    @Transactional
    public boolean upfollwing(User loginUser, Long id){
        User targetuser = findById(loginUser.getId());
        User user = findById(id);
        if(targetuser.getId().equals(user.getId())){
            return false;
        }

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
        return true;
    }

    @Override
    @Transactional
    public boolean checkFirstLogin(Long userId){
        User user = findById(userId);
        if(user.getfirst()){
            user.updateFirst();
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public User updateNickname(Long userId, String nickname){
        if(userRepository.findByNickname(nickname).isPresent()){
            throw new IllegalArgumentException("이미 사용중인 닉네임 입니다");
        } else {
            User user = userRepository.findById(userId).orElseThrow();
            return user.update(nickname);
        }
    }

    @Override
    @Transactional
    public User updateWriter(Long userId, String writer){
        return findById(userId).updateWriter(writer);
    }

    @Override
    @Transactional
    public User updateProfileUrl(Long userId, String url){
        if(!PROFILE_NAME.matcher(url).matches()){
            throw new IllegalArgumentException("올바른 url을 입력해주세요");
        }
        return findById(userId).UpdateProfileUrl(url);
    }

    @Override
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
