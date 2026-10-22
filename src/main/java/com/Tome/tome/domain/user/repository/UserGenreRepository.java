package com.Tome.tome.domain.user.repository;

import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.entity.UserGenre;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface UserGenreRepository extends JpaRepository<UserGenre, Long> {
    Optional<UserGenre> findByUserAndName(User user, String name);
    List<UserGenre> findByUser_id(Long id);
    Optional<UserGenre> findByName(String name);
    List<UserGenre> findTop3ByUserOrderByCountDesc(User user);
}