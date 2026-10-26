package com.Tome.tome.domain.book.dto;

import com.Tome.tome.domain.book.entity.Book;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

/** 엔티티를 직접 노출하지 않고 도서 정보와 추천 이유를 전달하는 응답. */
@Schema(description = "도서 정보 및 개인화 추천 이유")
@Getter

public class BookResponseDto {
    /** 도서 DB ID. */
    @Schema(description = "도서 DB ID", example = "1")
    private Long id;
    /** 표준 ISBN13. */
    @Schema(description = "표준 ISBN13", example = "9788983921987")
    private String isbn13;
    /** 도서명. */
    @Schema(description = "도서명", example = "예시 도서")
    private String title;
    /** 저자. 여러 명은 쉼표로 연결. */
    @Schema(description = "저자. 여러 명은 쉼표로 연결", example = "예시 작가")
    private String writer;
    /** 표지 이미지 URL. 없는 경우 빈 값 또는 null. */
    @Schema(description = "표지 이미지 URL. 없는 경우 빈 값 또는 null", example = "https://example.org/cover.jpg")
    private String bookurl;
    /** 도서 상세 링크. 직접 구매를 보장하는 링크가 아님. */
    @Schema(description = "도서 상세 링크. 직접 구매를 보장하는 링크가 아님", example = "https://example.org/book")
    private String bookbuyurl;
    /** 출판사. */
    @Schema(description = "출판사", example = "예시 출판사")
    private String publisher;
    /** 표준 장르 이름. 분류가 없으면 기타. */
    @Schema(description = "표준 장르 이름. 분류가 없으면 기타", example = "소설")
    private String genre;
    /** 도서 소개. 제공되지 않을 수 있음. */
    @Schema(description = "도서 소개. 제공되지 않을 수 있음", example = "예시 소개")
    private String description;
    /** 정보 제공처. 기존 데이터는 null일 수 있음. */
    @Schema(description = "정보 제공처. 기존 데이터는 null일 수 있음", example = "DATA4LIBRARY+KAKAO")
    private String source;
    /** 수집 기간의 대출 횟수. 없는 경우 0. */
    @Schema(description = "수집 기간의 대출 횟수. 없는 경우 0", example = "123")
    private long loanCount;
    /** 이 도서를 추천한 근거. */
    @Schema(description = "이 도서를 추천한 근거", example = "관심 장르인 '소설'에 맞는 도서입니다.")
    private String recommendationReason;

    /** 엔티티의 nullable 필드를 안전하게 응답으로 변환한다. 추천용 생성자는 여기에 추천 이유를 추가한다. */
    public BookResponseDto(Book book) {
        this.id = book.getId();
        this.isbn13 = book.getIsbn13();
        this.title = book.getTitle();
        this.writer = book.getWriter();
        this.bookurl = book.getBookurl();
        this.bookbuyurl = book.getBookbuyurl();
        this.publisher = book.getPublisher();
        this.genre = book.getGenre() == null ? "기타" : book.getGenre().getName();
        this.description = book.getDescription();
        this.source = book.getSource();
        this.loanCount = book.getLoanCount() == null ? 0 : book.getLoanCount();
    }
    /** 엔티티의 nullable 필드를 안전하게 응답으로 변환한다. 추천용 생성자는 여기에 추천 이유를 추가한다. */
    public BookResponseDto(Book book, String reason) {
        this(book);
        this.recommendationReason = reason;
    }
}
