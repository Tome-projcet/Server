package com.Tome.tome.domain.user.repository;

import com.Tome.tome.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User u set u.follow = u.follow + :delta where u.id = :id")
    int addFollow(@Param("id") Long id, @Param("delta") int delta);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User u set u.follwing = u.follwing + :delta where u.id = :id")
    int addFollowing(@Param("id") Long id, @Param("delta") int delta);
}
