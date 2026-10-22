package com.Tome.tome.domain.book.service;

import com.Tome.tome.domain.book.dto.BookResponseDto;
import com.Tome.tome.domain.book.entity.Book;
import com.Tome.tome.domain.book.port.in.RecommendationUseCase;
import com.Tome.tome.domain.book.repository.BookRepository;
import com.Tome.tome.domain.user.entity.UserGenre;
import com.Tome.tome.domain.user.repository.UserGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService implements RecommendationUseCase {

    private static final int RECOMMEND_LIMIT = 10;

    private final BookRepository bookRepository;
    private final UserGenreRepository userGenreRepository;

    @Override
    public List<BookResponseDto> getRecommendations(Long userId) {

        List<UserGenre> userGenres = new ArrayList<>(userGenreRepository.findByUser_id(userId));

        userGenres.sort(
                Comparator.comparingInt(UserGenre::getCount).reversed()
        );

        List<BookResponseDto> recommendations = new ArrayList<>();

        for (UserGenre userGenre : userGenres) {
            int remaining = RECOMMEND_LIMIT - recommendations.size();
            if (remaining <= 0) {
                break;
            }

            List<Book> books =
                    bookRepository.findByGenre_Name(userGenre.getName(), PageRequest.of(0, remaining));

            for (Book book : books) {
                recommendations.add(new BookResponseDto(book));
            }
        }

        return recommendations;
    }
}
