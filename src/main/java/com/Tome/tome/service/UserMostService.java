package com.Tome.tome.service;


import com.Tome.tome.domain.User;
import com.Tome.tome.domain.UserGenre;
import com.Tome.tome.domain.UserMostGenre;
import com.Tome.tome.repository.UserGenreRepository;
import com.Tome.tome.repository.UserMostGenreRepository;
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



    @Transactional
    public void mostGenreset(User user){
        List<UserGenre> top3 = userGenreRepository.findTop3ByUserOrderByCountDesc(user);
        UserGenre first = top3.get(0);
        UserGenre second = top3.size() > 1 ? top3.get(1) : null;
        UserGenre third = top3.size() > 2 ? top3.get(2) : null;

        UserMostGenre userMostGenre = userMostGenreRepository.findByUser_Id(user.getId()).orElseThrow(() -> new IllegalArgumentException("not found" + user.getId()));

        userMostGenre.setFirst(first.getName());
        userMostGenre.setSecond(second.getName());
        userMostGenre.setThird(third.getName());
    }
}
