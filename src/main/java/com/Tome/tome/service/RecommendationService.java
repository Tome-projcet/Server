package com.Tome.tome.service;

import com.Tome.tome.domain.Book;
import com.Tome.tome.domain.UserGenre;
import com.Tome.tome.dto.BookResponseDto;
import com.Tome.tome.repository.BookRepository;
import com.Tome.tome.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;

    public List<BookResponseDto> getRecommendations(Long userId) {

        List<UserGenre> userGenres = genreRepository.findByUser_id(userId);

        userGenres.sort(
                Comparator.comparingInt(UserGenre::getCount).reversed()
        );

        List<BookResponseDto> recommendations = new ArrayList<>();

        for (UserGenre userGenre : userGenres) {

            List<Book> books =
                    bookRepository.findByGenre_Name(userGenre.getName());

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