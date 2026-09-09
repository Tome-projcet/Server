package com.Tome.tome.service;

import com.Tome.tome.domain.Book;
import com.Tome.tome.domain.Genre;
import com.Tome.tome.dto.BookResponseDto;
import com.Tome.tome.repository.BookRepository;
import com.Tome.tome.repository.UserPreferenceGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final BookRepository bookRepository;
    private final UserPreferenceGenreRepository userPreferenceGenreRepository;

    public List<BookResponseDto> getRecommendations(Long userId) {

        List<BookResponseDto> recommendations = new ArrayList<>();

        List<Genre> preferredGenres =
                userPreferenceGenreRepository.findPreferredGenresByUserId(userId);

        if (preferredGenres.isEmpty()) {
            return recommendations;
        }

        for (Genre genre : preferredGenres) {

            List<Book> books = bookRepository.findByGenre(genre);

            for (Book book : books) {
                recommendations.add(new BookResponseDto(book));
            }
        }

        if (recommendations.size() > 10) {
            return recommendations.subList(0, 10);
        }

        return recommendations;
    }
}