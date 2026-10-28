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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService implements UserUseCase {
    private final UserRepository userRepository;
    private final UserGenreRepository userGenreRepository;
    private final UserFollowRepository userFollowRepository;
    private final PasswordEncoder passwordEncoder;

    private final static Pattern PROFILE_NAME = Pattern.compile("^https?://\\S+$");

    @Override
    public Long save(AddUserRequest dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다");
        }
        if (dto.getNickname() == null || dto.getNickname().isBlank()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요");
        }

        Optional<User> result = userRepository.findByEmail(dto.getEmail());
        if (result.isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다");
        } else {
            result = userRepository.findByNickname(dto.getNickname());
            if (result.isPresent()) {
                throw new IllegalArgumentException("이미 사용중인 닉네임 입니다");
            } else {
                return userRepository.save(User.builder().email(dto.getEmail()).nickname(dto.getNickname()).password(passwordEncoder.encode(dto.getPassword())).build()).getId();
            }
        }
    }

    @Override
    public User findById(Long userId){
        return userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다"));
    }

    @Override
    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다"));
    }

    @Override
    @Transactional
    public boolean upfollwing(User loginUser, Long id){
        User targetuser = findById(loginUser.getId());
        User user = findById(id);
        if(targetuser.getId().equals(user.getId())){
            throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다");
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
    public boolean isFirstLogin(Long userId){
        return findById(userId).getfirst();
    }

    @Override
    @Transactional
    public void completeFirstLogin(Long userId){
        findById(userId).updateFirst();
    }

    @Override
    @Transactional
    public User updateNickname(Long userId, String nickname){
        if(nickname == null || nickname.isBlank()){
            throw new IllegalArgumentException("닉네임을 입력해주세요");
        }
        User user = findById(userId);
        if(nickname.equals(user.getNickname())){
            throw new IllegalArgumentException("동일한 닉네임 입니다");
        }
        if(userRepository.findByNickname(nickname).isPresent()){
            throw new IllegalArgumentException("이미 사용중인 닉네임 입니다");
        }
        return user.update(nickname);

    }

    @Override
    @Transactional
    public User updateWriter(Long userId, String writer){
        if(writer == null || writer.isBlank()){
            throw new IllegalArgumentException("인생 작가를 입력해주세요");
        }
        return findById(userId).updateWriter(writer);
    }

    @Override
    @Transactional
    public User updateProfileUrl(Long userId, String url){
        if(url == null || !PROFILE_NAME.matcher(url).matches()){
            throw new IllegalArgumentException("올바른 url을 입력해주세요");
        }
        return findById(userId).UpdateProfileUrl(url);
    }

    @Override
    @Transactional
    public void checkGenre(User user, String genreName){
        if(genreName == null || genreName.isBlank()){
            throw new IllegalArgumentException("장르를 입력해주세요");
        }
        Optional<UserGenre> existing = userGenreRepository.findByUserAndName(user, genreName);

        if (existing.isPresent()) {
            existing.get().increaseCount();
        } else {
            userGenreRepository.save(UserGenre.builder().name(genreName).user(user).build());
        }
    }


}
