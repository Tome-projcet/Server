package com.Tome.tome.domain.user.controller.mypage;

import com.Tome.tome.domain.book.dto.BookResponseDto;
import com.Tome.tome.domain.user.dto.LifeBookRequest;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.service.LifeBookService;
import com.Tome.tome.domain.user.service.LifeBookStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** 현재 로그인 사용자의 인생 책만 변경한다. 다른 사용자의 ID를 요청 본문으로 받지 않는다. */
@Tag(name = "인생 책", description = "API 검색 도서를 온보딩·마이페이지의 인생 책으로 연결")
@RestController
@RequiredArgsConstructor
public class LifeBookController {
    private final LifeBookService service;
    private final LifeBookStore store;

    @Operation(summary = "인생 책 선택·변경", description = "책 검색 결과의 ISBN을 전달합니다. DB에 없으면 외부 API에서 가져와 저장하고 사용자와 연결합니다. ISBN10은 ISBN13으로 정규화하며 기존 책은 재사용합니다. Bearer JWT 인증이 필요합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "선택한 책 정보"),
            @ApiResponse(responseCode = "400", description = "ISBN 누락·형식 오류"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "책을 찾을 수 없음"),
            @ApiResponse(responseCode = "503", description = "API 이용 불가. 기존 인생 책은 유지")})
    @PutMapping({"/api/mypage/lifebook", "/api/onbording/book"})
    public BookResponseDto set(@Parameter(hidden = true) @AuthenticationPrincipal User user, @RequestBody LifeBookRequest request) {
        return service.set(user.getId(), request.isbn());
    }

    @Operation(summary = "내 인생 책 조회", description = "미설정 상태는 204를 반환합니다. API를 재호출하지 않고 저장된 책을 반환합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "인생 책 정보"), @ApiResponse(responseCode = "204", description = "선택한 인생 책 없음")})
    @GetMapping("/api/mypage/lifebook")
    public ResponseEntity<BookResponseDto> get(@Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return store.get(user.getId()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "인생 책 선택 해제", description = "현재 사용자와의 연결만 해제합니다. 책·독후감·다른 사용자의 선택은 삭제하지 않습니다.")
    @ApiResponse(responseCode = "204", description = "해제 완료. 이미 비어 있어도 성공")
    @DeleteMapping("/api/mypage/lifebook")
    public ResponseEntity<Void> clear(@Parameter(hidden = true) @AuthenticationPrincipal User user) {
        store.clear(user.getId());
        return ResponseEntity.noContent().build();
    }
}
