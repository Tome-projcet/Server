package com.Tome.tome.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/** main의 세션·JWT 인증을 그대로 사용하는 담당 기능 문서. */
@Configuration
@PropertySource("classpath:book-openapi.properties")
public class BookOpenApiConfig {
    /** 로그인 쿠키는 같은 서버의 브라우저가 자동 전송하며 API 키를 문서에 저장하지 않는다. */
    @Bean
    public OpenAPI bookOpenApi() {
        return new OpenAPI().info(new Info().title("Tome 도서·장르·추천 API").version("1.0")
                .description("카카오 도서 검색, 정보나루 인기 도서 수집, 관심 장르 관리와 개인화 추천. "
                        + "main의 로그인 세션 또는 Bearer JWT로 인증합니다. API 키는 서버의 .env에만 설정합니다. "
                        + "세션으로 PUT·POST 요청 시 XSRF-TOKEN 쿠키와 X-XSRF-TOKEN 헤더가 필요합니다. Swagger는 쿠키를 읽어 헤더를 채웁니다. 비로그인 요청은 302 이동합니다."))
                .components(new Components().addSecuritySchemes("loginSession", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE).name("JSESSIONID")
                        .description("기존 로그인 후 브라우저가 자동 전송하는 세션 쿠키. 여기서 직접 입력하지 않습니다."))
                        .addSecuritySchemes("bearerAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT").description("main의 /api/token에서 발급한 액세스 토큰")))
                .addSecurityItem(new SecurityRequirement().addList("loginSession"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /** 다른 팀원의 로그인·토큰 API와 담당 문서의 범위를 구분한다. */
    @Bean
    public GroupedOpenApi bookRecommendationApi() {
        return GroupedOpenApi.builder().group("books-and-recommendations")
                .displayName("도서 · 장르 · 추천")
                .pathsToMatch("/api/books/**", "/api/genres/**", "/api/recommend/**").build();
    }
}
