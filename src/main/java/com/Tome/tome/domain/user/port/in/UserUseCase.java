package com.Tome.tome.domain.user.port.in;

import com.Tome.tome.domain.user.dto.AddUserRequest;
import com.Tome.tome.domain.user.entity.User;

public interface UserUseCase {
    Long save(AddUserRequest dto);

    User findById(Long userId);

    User findByEmail(String email);

    boolean upfollwing(User loginUser, Long id);

    boolean isFirstLogin(Long userId);

    void completeFirstLogin(Long userId);

    User updateNickname(Long userId, String nickname);

    User updateWriter(Long userId, String writer);

    User updateProfileUrl(Long userId, String url);

    void checkGenre(User user, String genreName);
}
