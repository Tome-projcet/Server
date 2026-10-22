package com.Tome.tome.domain.user.service;

import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.entity.UserGenre;
import com.Tome.tome.domain.user.entity.UserMostGenre;
import com.Tome.tome.domain.user.repository.UserGenreRepository;
import com.Tome.tome.domain.user.repository.UserMostGenreRepository;
import com.Tome.tome.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserMostService {
    private final UserGenreRepository userGenreRepository;
    private final UserMostGenreRepository userMostGenreRepository;
    private final UserRepository userRepository;



    @Transactional
    public void mostGenreset(User user){
        List<UserGenre> top3 = userGenreRepository.findTop3ByUserOrderByCountDesc(user);
        UserGenre first = top3.size() > 0 ? top3.get(0) : null;
        UserGenre second = top3.size() > 1 ? top3.get(1) : null;
        UserGenre third = top3.size() > 2 ? top3.get(2) : null;

        UserMostGenre userMostGenre = userMostGenreRepository.findByUser_Id(user.getId())
                .orElseGet(() -> userMostGenreRepository.save(new UserMostGenre(
                        userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("not found" + user.getId())))));

        userMostGenre.setFirst(first != null ? first.getName() : null);
        userMostGenre.setSecond(second != null ? second.getName() : null);
        userMostGenre.setThird(third != null ? third.getName() : null);
    }
}
