package com.Tome.tome.catalog;

public record ExternalBook(String isbn13, String title, String writer, String coverUrl,
                           String detailUrl, String publisher, String description,
                           String genre, long loanCount, String source) {
    public ExternalBook enrichedWith(ExternalBook details) {
        if (details == null) return this;
        return new ExternalBook(isbn13, prefer(details.title, title), prefer(details.writer, writer),
                prefer(details.coverUrl, coverUrl), prefer(details.detailUrl, detailUrl),
                prefer(details.publisher, publisher), prefer(details.description, description),
                genre, loanCount, source + "+KAKAO");
    }
    private static String prefer(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
