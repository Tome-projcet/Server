package com.Tome.tome.domain.genre.config;

import com.Tome.tome.domain.book.service.BookCatalogStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** 서버 시작 시 공통 장르만 등록한다. 외부 API를 호출하거나 기존 장르를 삭제하지 않는다. */
@Component
@RequiredArgsConstructor
public class GenreInitializer implements ApplicationRunner {
    private final BookCatalogStore catalog;
    /** API 요청 전에 장르 목록을 준비한다. 이미 존재하는 이름은 다시 저장하지 않는다. */
    @Override
    public void run(ApplicationArguments args) { catalog.ensureGenres(); }
}
