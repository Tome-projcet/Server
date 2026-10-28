package com.Tome.tome.domain.book.port.in;

import com.Tome.tome.domain.book.dto.BookResponseDto;

import java.util.List;

public interface RecommendationUseCase {
    List<BookResponseDto> getRecommendations(Long userId);

    /** 추천 개수를 지정하며 기존 단일 인자 호출도 계속 지원한다. */
    List<BookResponseDto> getRecommendations(Long userId, int limit);
}
