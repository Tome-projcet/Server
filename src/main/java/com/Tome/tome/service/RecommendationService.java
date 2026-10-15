package com.Tome.tome.service;

import com.Tome.tome.catalog.GenreTaxonomy;
import com.Tome.tome.domain.Book;
import com.Tome.tome.domain.UserGenre;
import com.Tome.tome.dto.BookResponseDto;
import com.Tome.tome.repository.BookRepository;
import com.Tome.tome.repository.UserGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final GenreTaxonomy taxonomy;

    private final BookRepository bookRepository;
    private final UserGenreRepository userGenreRepository;

    @Transactional(readOnly = true)
    public List<BookResponseDto> getRecommendations(Long userId) {

        List<UserGenre> userGenres = userGenreRepository.findByUser_id(userId);

        return rank(userGenres, 10);
    }

    @Transactional(readOnly = true)
    public List<BookResponseDto> getRecommendations(Long userId, int limit) {
        if (limit < 1 || limit > 30) throw new IllegalArgumentException("추천 개수는 1~30권입니다.");
        return rank(userGenreRepository.findByUser_id(userId), limit);
    }

    private List<BookResponseDto> rank(List<UserGenre> userGenres, int limit) {
        Map<String, Integer> preferences = new HashMap<>();
        for (UserGenre genre : userGenres) {
            if (taxonomy.supported(genre.getName()))
                preferences.merge(taxonomy.canonical(genre.getName()), Math.max(1, genre.getCount()), Integer::sum);
        }
        Map<String, Book> candidates = new LinkedHashMap<>();
        // Bounded DB queries; recommendation never depends on live external API availability.
        List<String> preferred = preferences.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(5).map(Map.Entry::getKey).toList();
        for (String name : preferred)
            add(candidates, bookRepository.findByGenre_NameIn(taxonomy.aliases(name), PageRequest.of(0, 60)));
        add(candidates, bookRepository.findAllBy(PageRequest.of(0, 100)));
        if (candidates.isEmpty()) return List.of();
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
        int perGenre = Math.max(1, (int) Math.ceil(limit * 0.4));
        for (Book book : ranked) {
            if (genreCounts.getOrDefault(genre(book), 0) >= perGenre) continue;
            selected.add(book); used.add(book.getIsbn13());
            genreCounts.merge(genre(book), 1, Integer::sum);
            if (selected.size() == limit) break;
        }
        // If the catalog has only one genre, fill the remaining slots rather than returning an arbitrary shortage.
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
    private void add(Map<String, Book> result, List<Book> books) {
        for (Book book : books)
            if (book.getIsbn13() != null && !book.getIsbn13().isBlank()) result.putIfAbsent(book.getIsbn13(), book);
    }
    private String genre(Book book) { return taxonomy.canonical(book.getGenre() == null ? null : book.getGenre().getName()); }
    private static long loans(Book book) { return book.getLoanCount() == null ? 0 : Math.max(0, book.getLoanCount()); }

}
