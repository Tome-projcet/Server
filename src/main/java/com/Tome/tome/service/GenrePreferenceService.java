package com.Tome.tome.service;

import com.Tome.tome.catalog.GenreTaxonomy;
import com.Tome.tome.domain.*;
import com.Tome.tome.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GenrePreferenceService {
    private final UserGenreRepository preferences;
    private final UserRepository users;
    private final GenreTaxonomy taxonomy;
    @Transactional(readOnly = true)
    public List<Preference> get(Long userId) {
        return preferences.findByUser_id(userId).stream().map(p -> new Preference(taxonomy.canonical(p.getName()), p.getCount())).toList();
    }
    @Transactional
    public List<Preference> replace(Long userId, List<String> names) {
        if (names == null || names.size() > 5 || names.stream().anyMatch(n -> !taxonomy.supported(n) || "기타".equals(taxonomy.canonical(n))))
            throw new IllegalArgumentException("관심 장르는 제공된 목록에서 최대 5개 선택하세요.");
        Set<String> selected = new LinkedHashSet<>();
        names.forEach(n -> selected.add(taxonomy.canonical(n)));
        // Lock the user row to serialize replacement requests for the same account.
        User user = users.findByIdForGenreUpdate(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<UserGenre> existing = preferences.findByUser_id(userId);
        Set<String> retained = new HashSet<>();
        for (UserGenre preference : existing) {
            String name = taxonomy.canonical(preference.getName());
            if (!selected.contains(name) || !retained.add(name)) preferences.delete(preference);
        }
        for (String name : selected) if (!retained.contains(name)) {
            UserGenre preference = new UserGenre(name, user);
            preference.increaseCount();
            preferences.save(preference);
        }
        return get(userId);
    }
    public record Preference(String name, int count) {}
}
