package com.Tome.tome.config;

import com.Tome.tome.service.BookCatalogStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenreInitializer implements ApplicationRunner {
    private final BookCatalogStore catalog;
    @Override
    public void run(ApplicationArguments args) { catalog.ensureGenres(); }
}
