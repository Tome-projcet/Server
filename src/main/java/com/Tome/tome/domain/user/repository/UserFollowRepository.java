package com.Tome.tome.domain.user.repository;

import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {
    Optional<UserFollow> findByUserAndMainUser(User user, User mainUser);

    void deleteByMainUserAndUser(User mainUser, User user);
}
