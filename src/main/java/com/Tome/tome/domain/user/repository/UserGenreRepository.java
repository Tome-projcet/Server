package com.Tome.tome.domain.user.repository;

import com.Tome.tome.domain.user.entity.UserGenre;
import com.Tome.tome.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

/** main의 장르 관련 서비스가 사용하는 조회 계약을 보존한다. 추천에서는 사용자 ID 조건을 반드시 적용한다. */
public interface UserGenreRepository extends JpaRepository<UserGenre, Long> {
    /** 현재 사용자와 이름이 모두 일치하는 선호 기록을 조회한다. */
    Optional<UserGenre> findByUserAndName(User user, String name);
    /** 추천을 위해 특정 사용자의 관심 장르만 조회한다. */
    List<UserGenre> findByUser_id(Long id);
    /** 기존 main 호출 호환용 조회다. 개인화 추천에는 사용자 조건이 있는 메서드를 사용한다. */
    Optional<UserGenre> findByName(String name);
    /** 특정 사용자의 선호 카운트 상위 3개를 조회한다. */
    List<UserGenre> findTop3ByUserOrderByCountDesc(User user);
}
