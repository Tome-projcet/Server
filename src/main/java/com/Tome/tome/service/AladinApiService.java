package com.Tome.tome.service;

import com.Tome.tome.domain.Book;
import com.Tome.tome.domain.Genre;
import com.Tome.tome.repository.BookRepository;
import com.Tome.tome.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AladinApiService {

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String ttbKey = "ttbtor1091936001";

    public void fetchAndSaveBooks() {

        String url = UriComponentsBuilder
                .fromUriString("http://www.aladin.co.kr/ttb/api/ItemList.aspx")
                .queryParam("ttbkey", ttbKey)
                .queryParam("QueryType", "Bestseller")
                .queryParam("MaxResults", 10)
                .queryParam("start", 1)
                .queryParam("SearchTarget", "Book")
                .queryParam("output", "js")
                .queryParam("Version", "20131101")
                .toUriString();

        try {

            String response =
                    restTemplate.getForObject(url, String.class);

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode items =
                    root.get("item");

            if (items == null || !items.isArray()) {
                System.out.println("알라딘 도서 데이터가 없습니다.");
                return;
            }

            for (JsonNode item : items) {
                String isbn13 = getText(item, "isb13");
                String title = getText(item, "title");
                String writer = getText(item, "author");
                String categoryName = getText(item, "categoryName");
                String bookurl = getText(item, "cover");
                String bookbuyurl = getText(item, "link");
                String publisher = getText(item, "publisher");

                if (isbn13 == null || isbn13.isBlank()){
                    continue;
                }

                if (bookRepository.findByIsbn13(isbn13).isPresent()) {
                    continue;
                }

                String genreName = extractGenre(categoryName);

                Genre genre = genreRepository
                        .findByName(genreName)
                        .orElseGet(() ->
                                genreRepository.save(
                                        new Genre(genreName)
                                )
                        );

                Book book = new Book(
                        isbn13,
                        title,
                        writer,
                        bookurl,
                        bookbuyurl,
                        publisher,
                        genre
                );

                bookRepository.save(book);

                System.out.println(
                        "저장 완료: " + title
                                + " / 장르: " + genreName
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "알라딘 API 호출 실패: "
                            + e.getMessage()
            );
        }
    }
    private String extractGenre(String categoryName) {

        if (categoryName == null || categoryName.isBlank()) {
            return "기타";
        }

        String[] categories =
                categoryName.split(">");

        if (categories.length > 1) {
            return categories[1].trim();
        }

        return "기타";
    }

    private String getText(
            JsonNode item,
            String field
    ) {

        JsonNode value = item.get(field);

        if (value == null || value.isNull()) {
            return "";
        }

        return value.asText();
    }
}

