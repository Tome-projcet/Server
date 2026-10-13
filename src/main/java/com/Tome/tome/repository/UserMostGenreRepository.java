package com.Tome.tome.repository;

import com.Tome.tome.domain.UserMostGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMostGenreRepository extends JpaRepository<UserMostGenre, Long> {
    Optional<UserMostGenre> findByUser_Id(Long userId);
}
