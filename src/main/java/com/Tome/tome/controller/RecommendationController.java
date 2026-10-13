package com.Tome.tome.controller;

import com.Tome.tome.dto.BookResponseDto;
import com.Tome.tome.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/books")
    public List<BookResponseDto> getRecommendations(
            @RequestParam Long userId) {

        return recommendationService.getRecommendations(userId);
    }
}