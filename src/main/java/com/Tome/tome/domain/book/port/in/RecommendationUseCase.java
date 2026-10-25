package com.Tome.tome.domain.book.port.in;

import com.Tome.tome.domain.book.dto.BookResponseDto;

import java.util.List;

public interface RecommendationUseCase {
    List<BookResponseDto> getRecommendations(Long userId);
}
