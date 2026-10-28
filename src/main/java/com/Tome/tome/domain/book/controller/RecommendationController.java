package com.Tome.tome.domain.book.controller;

import com.Tome.tome.domain.book.dto.BookResponseDto;
import com.Tome.tome.domain.book.port.in.RecommendationUseCase;
import com.Tome.tome.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Recommendation", description = "도서 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationUseCase recommendationUseCase;

    @Operation(summary = "선호 장르 기반 도서 추천", description = "사용자가 고른 장르 횟수가 많은 순서로 최대 10권을 추천합니다.")
    @GetMapping("/books")
    public List<BookResponseDto> getRecommendations(@AuthenticationPrincipal User user) {

        return recommendationUseCase.getRecommendations(user.getId());
    }
}
