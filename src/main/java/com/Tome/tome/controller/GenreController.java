package com.Tome.tome.controller;

import com.Tome.tome.domain.User;
import com.Tome.tome.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {
    private final BookCatalogStore catalog;
    private final GenrePreferenceService preferences;
    @GetMapping
    public List<GenreResponse> genres() {
        return catalog.listGenres().stream().map(g -> new GenreResponse(g.getId(), g.getName())).toList();
    }
    @GetMapping("/preferences")
    public List<GenrePreferenceService.Preference> get(@AuthenticationPrincipal User user) { return preferences.get(id(user)); }
    @PutMapping("/preferences")
    public List<GenrePreferenceService.Preference> replace(@AuthenticationPrincipal User user, @RequestBody PreferenceRequest request) {
        return preferences.replace(id(user), request.names());
    }
    private Long id(User user) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        return user.getId();
    }
    public record GenreResponse(Long id, String name) {}
    public record PreferenceRequest(List<String> names) {}
}
