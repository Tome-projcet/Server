package com.Tome.tome.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import java.net.http.HttpClient;
import java.time.Duration;
import lombok.Getter;

/** 환경변수 또는 프로젝트 .env에서 공급자 키를 읽는다. 기본값은 빈 값이라 키 없이도 서버가 시작된다. 실제 키는 주석·문서·로그에 남기지 않는다. */
@Configuration
@PropertySource(value = "file:.env", ignoreResourceNotFound = true, encoding = "UTF-8")
@Getter
public class
BookApiConfig {
    @Value("${books.kakao.key:${KAKAO_REST_API_KEY:}}")
    private String kakaoKey;
    @Value("${books.library.key:${DATA4LIBRARY_API_KEY:}}")
    private String libraryKey;
    @Value("${books.kakao.url:https://dapi.kakao.com/v3/search/book}")
    private String kakaoUrl;
    @Value("${books.library.url:https://data4library.kr/api}")
    private String libraryUrl;
    @Value("${books.sync-token:${BOOK_SYNC_TOKEN:}}")
    private String syncToken;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3)).build();
}
