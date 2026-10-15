package com.Tome.tome.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import java.net.http.HttpClient;
import java.time.Duration;
import lombok.Getter;

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
