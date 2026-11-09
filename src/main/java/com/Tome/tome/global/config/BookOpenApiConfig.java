package com.Tome.tome.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/** 공통 SwaggerConfig의 JWT 보안 설명을 사용하며, 도서·장르·추천·인생 책 문서를 묶는다. */
@Configuration
@PropertySource("classpath:book-openapi.properties")
public class BookOpenApiConfig {
    @Bean
    public GroupedOpenApi bookRecommendationApi() {
        return GroupedOpenApi.builder().group("books-and-recommendations")
                .displayName("도서 · 장르 · 추천 · 인생 책")
                .pathsToMatch("/api/books/**", "/api/genres/**", "/api/recommend/**", "/api/mypage/lifebook", "/api/onbording/book").build();
    }
}
