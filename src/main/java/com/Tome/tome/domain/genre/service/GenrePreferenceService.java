package com.Tome.tome.domain.genre.service;

import com.Tome.tome.domain.book.infra.GenreTaxonomy;
import com.Tome.tome.domain.book.entity.Book;
import com.Tome.tome.domain.genre.entity.Genre;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.entity.UserGenre;
import com.Tome.tome.domain.book.repository.BookRepository;
import com.Tome.tome.domain.genre.repository.GenreRepository;
import com.Tome.tome.domain.user.repository.UserRepository;
import com.Tome.tome.domain.user.repository.UserGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** 관심 장르 교체와 선호 카운트를 관리한다. main의 UserGenre 이름·카운트 구조를 유지한다. */
@Service
@RequiredArgsConstructor
public class GenrePreferenceService {
    private final UserGenreRepository preferences;
    private final UserRepository users;
    private final GenreTaxonomy taxonomy;
    /** 현재 사용자에게 저장된 관심 장르를 표준 이름으로 변환해 반환한다. */
    @Transactional(readOnly = true)
    public List<Preference> get(Long userId) {
        return preferences.findByUser_id(userId).stream().map(p -> new Preference(taxonomy.canonical(p.getName()), p.getCount())).toList();
    }
    /** 선택을 검증하고 중복을 정리한다. 유지한 장르의 카운트는 남기고 새 선택의 카운트는 1로 시작한다. */
    @Transactional
    public List<Preference> replace(Long userId, List<String> names) {
        if (names == null || names.size() > 5 || names.stream().anyMatch(n -> !taxonomy.supported(n) || "기타".equals(taxonomy.canonical(n))))
            throw new IllegalArgumentException("관심 장르는 제공된 목록에서 최대 5개 선택하세요.");
        Set<String> selected = new LinkedHashSet<>();
        names.forEach(n -> selected.add(taxonomy.canonical(n)));
        // 같은 사용자의 교체 요청을 직렬화해 중복 저장과 선택 덮어쓰기를 방지한다.
        User user = users.findByIdForGenreUpdate(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<UserGenre> existing = preferences.findByUser_id(userId);
        Set<String> retained = new HashSet<>();
        for (UserGenre preference : existing) {
            String name = taxonomy.canonical(preference.getName());
            if (!selected.contains(name) || !retained.add(name)) preferences.delete(preference);
            else preference.rename(name); // 응답뿐 아니라 DB 이름도 맞춰 main의 checkGenre가 같은 행을 갱신하게 한다.
        }
        for (String name : selected) if (!retained.contains(name)) {
            UserGenre preference = new UserGenre(name, user);
            preference.increaseCount();
            preferences.save(preference);
        }
        return get(userId);
    }
    @io.swagger.v3.oas.annotations.media.Schema(description = "사용자 관심 장르와 선호 강도")
    public record Preference(@io.swagger.v3.oas.annotations.media.Schema(description = "표준 장르 이름", example = "소설") String name,
                             @io.swagger.v3.oas.annotations.media.Schema(description = "선호 카운트. 기존 0도 관심 선택으로 처리", example = "3") int count) {}
}
