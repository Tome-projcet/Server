package com.Tome.tome.domain.book.service;

import com.Tome.tome.domain.book.infra.GenreTaxonomy;
import com.Tome.tome.domain.book.entity.Book;
import com.Tome.tome.domain.user.entity.UserGenre;
import com.Tome.tome.domain.book.dto.BookResponseDto;
import com.Tome.tome.domain.book.repository.BookRepository;
import com.Tome.tome.domain.user.repository.UserGenreRepository;
import lombok.RequiredArgsConstructor;
import com.Tome.tome.domain.book.port.in.RecommendationUseCase;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** 저장된 도서만 사용해 개인화 추천을 만든다. 외부 API 장애가 추천 조회로 전파되지 않는다. */
@Service
@RequiredArgsConstructor
public class RecommendationService implements RecommendationUseCase {
    private final GenreTaxonomy taxonomy;

    private final BookRepository bookRepository;
    private final UserGenreRepository userGenreRepository;

    /** 사용자 관심 장르를 조회하고 추천 목록을 만든다. 개수 생략 시 10권, 지정 시 1~30권을 허용한다. */
    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDto> getRecommendations(Long userId) {

        List<UserGenre> userGenres = userGenreRepository.findByUser_id(userId);

        return rank(userGenres, 10);
    }

    /** 사용자 관심 장르를 조회하고 추천 목록을 만든다. 개수 생략 시 10권, 지정 시 1~30권을 허용한다. */
    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDto> getRecommendations(Long userId, int limit) {
        if (limit < 1 || limit > 30) throw new IllegalArgumentException("추천 개수는 1~30권입니다.");
        return rank(userGenreRepository.findByUser_id(userId), limit);
    }

    /** 별칭 선호도를 합치고 후보를 제한한다. 취향 75%·인기도 25% 점수와 장르 배분으로 최종 목록을 선택한다. */
    private List<BookResponseDto> rank(List<UserGenre> userGenres, int limit) {
        Map<String, Integer> preferences = new HashMap<>();
        for (UserGenre genre : userGenres) {
            if (taxonomy.supported(genre.getName()))
                preferences.merge(taxonomy.canonical(genre.getName()), Math.max(1, genre.getCount()), Integer::sum);
        }
        Map<String, Book> candidates = new LinkedHashMap<>();
        // 조회량을 제한한다: 관심 장르 최대 5개 × 60권 + 기본 인기 후보 100권.
        List<String> preferred = preferences.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(5).map(Map.Entry::getKey).toList();
        for (String name : preferred)
            add(candidates, bookRepository.findByGenre_NameIn(taxonomy.aliases(name), PageRequest.of(0, 60)));
        add(candidates, bookRepository.findAllBy(PageRequest.of(0, 100)));
        if (candidates.isEmpty()) return List.of();
        // 선호도와 대출량을 각각 정규화해 카운트 규모 차이가 점수를 지배하지 않도록 한다.
        int maxPreference = preferences.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        double maxPopularity = candidates.values().stream().mapToDouble(b -> Math.log1p(loans(b))).max().orElse(1);
        double denominator = Math.max(1, maxPopularity);
        List<Book> ranked = candidates.values().stream().sorted(Comparator
                .<Book>comparingDouble(book -> 0.75 * preferences.getOrDefault(genre(book), 0) / maxPreference
                        + 0.25 * Math.log1p(loans(book)) / denominator).reversed()
                .thenComparing(Book::getIsbn13)).toList();
        List<Book> selected = new ArrayList<>();
        Set<String> used = new HashSet<>();
        Map<String, Integer> genreCounts = new HashMap<>();
        // 첫 선택에서는 한 장르를 최대 40%까지 담는다. 후보 부족 시에는 아래 단계에서 완화한다.
        int perGenre = Math.max(1, (int) Math.ceil(limit * 0.4));
        for (Book book : ranked) {
            if (genreCounts.getOrDefault(genre(book), 0) >= perGenre) continue;
            selected.add(book); used.add(book.getIsbn13());
            genreCounts.merge(genre(book), 1, Integer::sum);
            if (selected.size() == limit) break;
        }
        // 다양성 제한으로 자리가 남으면 관심 밖의 장르를 먼저 보완하고, 그래도 부족하면 제한을 푼다.
        List<Book> overflow = new ArrayList<>(ranked);
        overflow.sort(Comparator.comparingInt(book -> preferences.containsKey(genre(book)) ? 1 : 0));
        for (Book book : overflow) {
            if (selected.size() == limit) break;
            if (used.add(book.getIsbn13())) selected.add(book);
        }
        return selected.stream().map(book -> new BookResponseDto(book, preferences.containsKey(genre(book))
                ? "관심 장르인 '" + genre(book) + "'에 맞는 도서입니다."
                : loans(book) > 0 ? "도서관에서 많이 대출된 도서입니다." : "서재에서 새로운 분야를 발견해 보세요.")).toList();
    }
    /** ISBN 기준으로 후보를 합쳐 같은 책이 여러 경로에서 들어와도 한 번만 추천한다. */
    private void add(Map<String, Book> result, List<Book> books) {
        for (Book book : books)
            if (book.getIsbn13() != null && !book.getIsbn13().isBlank()) result.putIfAbsent(book.getIsbn13(), book);
    }
    /** 기존 장르 이름을 표준 이름으로 맞춘다. 관계가 없는 기존 행은 기타로 처리한다. */
    private String genre(Book book) { return taxonomy.canonical(book.getGenre() == null ? null : book.getGenre().getName()); }
    /** 대출량이 없는 기존 데이터는 0으로 취급한다. */
    private static long loans(Book book) { return book.getLoanCount() == null ? 0 : Math.max(0, book.getLoanCount()); }

}
