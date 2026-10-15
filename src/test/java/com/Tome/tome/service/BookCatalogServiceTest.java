package com.Tome.tome.service;

import com.Tome.tome.catalog.*;
import com.Tome.tome.config.BookApiConfig;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookCatalogServiceTest {
    @Test void failedEnrichmentStillSavesLibraryBooksAndStopsFurtherRequests() {
        var kakao = mock(KakaoBookClient.class);
        var library = mock(LibraryBookClient.class);
        var store = mock(BookCatalogStore.class);
        var config = mock(BookApiConfig.class);
        when(config.getKakaoKey()).thenReturn("configured");
        var first = new ExternalBook("9788983921987", "책1", "", "", "", "", "", "소설", 30, "DATA4LIBRARY");
        var second = new ExternalBook("9788983921994", "책2", "", "", "", "", "", "인문", 20, "DATA4LIBRARY");
        var start = LocalDate.of(2026, 9, 1); var end = LocalDate.of(2026, 9, 30);
        when(library.popular(start, end, 10)).thenReturn(List.of(first, second));
        when(kakao.findByIsbn(first.isbn13())).thenThrow(new BookApiException("quota"));
        when(store.upsert(List.of(first, second))).thenReturn(2);
        var result = new BookCatalogService(kakao, library, store, config, mock(com.Tome.tome.repository.BookRepository.class)).syncPopular(start, end, 10);
        assertEquals(2, result.saved()); assertEquals(1, result.warnings().size());
        verify(kakao, never()).findByIsbn(second.isbn13());
        verify(store).upsert(List.of(first, second));
    }
    @Test void libraryFailureLeavesPreviousCatalogUntouched() {
        var library = mock(LibraryBookClient.class);
        var store = mock(BookCatalogStore.class);
        when(library.popular(any(), any(), anyInt())).thenThrow(new BookApiException("offline"));
        var service = new BookCatalogService(mock(KakaoBookClient.class), library, store, mock(BookApiConfig.class), mock(com.Tome.tome.repository.BookRepository.class));
        assertThrows(BookApiException.class, () -> service.syncPopular(LocalDate.now(), LocalDate.now(), 10));
        verifyNoInteractions(store);
    }
}
