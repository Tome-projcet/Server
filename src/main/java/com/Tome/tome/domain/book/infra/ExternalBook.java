package com.Tome.tome.domain.book.infra;

import io.swagger.v3.oas.annotations.media.Schema;

/** 두 공급자의 서로 다른 응답을 하나의 도서 형식으로 정규화한 값. */
@Schema(description = "검색 또는 통합 상세 도서 정보. 카카오 단독 응답의 장르는 기타")
public record ExternalBook(@Schema(description = "검증된 ISBN13") String isbn13, @Schema(description = "도서 제목") String title, @Schema(description = "저자 목록을 합친 문자열") String writer, @Schema(description = "표지 이미지 URL") String coverUrl,
                           @Schema(description = "도서 상세 페이지 URL. 구매 링크 보장 없음") String detailUrl, @Schema(description = "출판사") String publisher, @Schema(description = "도서 소개") String description,
                           @Schema(description = "표준 장르 이름. 분류가 없으면 기타") String genre, @Schema(description = "대출 횟수. 정보가 없으면 0") long loanCount, @Schema(description = "정보 제공처: KAKAO 또는 DATA4LIBRARY 또는 DATA4LIBRARY+KAKAO") String source) {
    /** 정보나루의 분류·대출량은 유지하면서 카카오의 비어 있지 않은 제목·저자·표지·소개로 보완한다. */
    public ExternalBook enrichedWith(ExternalBook details) {
        if (details == null) return this;
        return new ExternalBook(isbn13, prefer(details.title, title), prefer(details.writer, writer),
                prefer(details.coverUrl, coverUrl), prefer(details.detailUrl, detailUrl),
                prefer(details.publisher, publisher), prefer(details.description, description),
                genre, loanCount, source + "+KAKAO");
    }
    /** 보완 값이 없으면 기존 값을 유지해 이미 확보한 메타데이터가 지워지지 않게 한다. */
    private static String prefer(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
