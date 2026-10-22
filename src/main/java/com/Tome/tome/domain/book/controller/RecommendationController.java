package com.Tome.tome.domain.book.controller;

import com.Tome.tome.domain.book.dto.BookResponseDto;
import com.Tome.tome.domain.book.port.in.RecommendationUseCase;
import com.Tome.tome.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationUseCase recommendationUseCase;

    @GetMapping("/books")
    public List<BookResponseDto> getRecommendations(@AuthenticationPrincipal User user) {

        return recommendationUseCase.getRecommendations(user.getId());
    }
}
