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
    /** 관심 장르 교체 요청을 사용자 단위로 직렬화한다. main의 팔로우 갱신 메서드는 유지한다. */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForGenreUpdate(@Param("id") Long id);
}
