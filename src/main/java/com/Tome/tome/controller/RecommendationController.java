package com.Tome.tome.controller;

import com.Tome.tome.domain.User;
import com.Tome.tome.dto.BookResponseDto;
import com.Tome.tome.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendationController {
    private final RecommendationService recommendationService;
    @GetMapping("/books")
    public List<BookResponseDto> getRecommendations(@AuthenticationPrincipal User user,
            @RequestParam(required = false) Long userId, @RequestParam(defaultValue = "10") int limit) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        if (userId != null && !userId.equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 추천만 조회할 수 있습니다.");
        return recommendationService.getRecommendations(user.getId(), limit);
    }
}
