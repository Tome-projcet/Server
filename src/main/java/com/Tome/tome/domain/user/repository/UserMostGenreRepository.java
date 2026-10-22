package com.Tome.tome.domain.user.repository;

import com.Tome.tome.domain.user.entity.UserMostGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMostGenreRepository extends JpaRepository<UserMostGenre, Long> {
    Optional<UserMostGenre> findByUser_Id(Long userId);
}
