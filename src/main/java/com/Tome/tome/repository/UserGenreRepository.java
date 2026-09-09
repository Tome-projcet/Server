package com.Tome.tome.repository;

import com.Tome.tome.domain.UserGenre;
import com.Tome.tome.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface UserGenreRepository extends JpaRepository<UserGenre, Long> {
    Optional<UserGenre> findByUserAndGenreName(User user, String genreName);
    List<UserGenre> findByUser_id(Long id);
    Optional<UserGenre> findByName(String name);

}
