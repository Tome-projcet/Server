package com.Tome.tome.controller;

import com.Tome.tome.service.AladinApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/aladin")

public class AladinApiController {

    private final AladinApiService aladinApiService;
    @PostMapping("/books")
    public String fetchBook() {

        aladinApiService.fetchAndSaveBooks();

        return "알라딘 도서 저장";
    }
}
