# API 도서 → 인생 책 연동 (2026-11-02)

기준은 devlop 651cc74이며 IntelliJ의 /Users/dgsw41/Desktop/Server에서 수정했다. main 브랜치는 변경하지 않는다. Git 커밋·푸시도 실행하지 않는다.

## 사용 흐름

1. 로그인 후 accessToken을 받는다.
2. GET /api/books/search?query=데미안 으로 책을 검색한다.
3. 응답의 isbn13을 선택하고 아래 요청을 보낸다.

```http
PUT /api/mypage/lifebook
Authorization: Bearer <accessToken>
Content-Type: application/json

{"isbn":"9788983921987"}
```

온보딩에서도 PUT /api/onbording/book에 같은 본문을 보낸다. 기존 프로젝트의 onbording 경로 표기를 유지했다.
성공하면 200과 BookResponseDto(도서 ID·ISBN·제목·저자·표지·소개·대표 장르 등)를 반환한다.

| API | 결과 |
|---|---|
| PUT /api/mypage/lifebook | 현재 사용자 인생 책 설정·교체 |
| PUT /api/onbording/book | 온보딩 인생 책 설정. 위 API와 동일한 동작 |
| GET /api/mypage/lifebook | 선택된 책 200, 미설정이면 본문 없는 204 |
| DELETE /api/mypage/lifebook | 연결만 해제하고 204. 여러 번 호출해도 성공 |
| GET /api/mypage | 기존 응답에 lifeBook 추가. 미선택이면 null |
| GET /api/otherpage/{id} | 기존 공개 프로필 응답에 lifeBook 추가 |

모든 인생 책 API는 로그인한 사용자 ID를 사용하며 본문으로 변경 대상 사용자 ID를 받지 않는다. 비로그인은 401이다.

## 저장 방식

- User.lifeBook은 Book을 참조하는 선택 관계이며 DB 컬럼은 users.life_book_id다. 여러 사용자가 같은 책을 선택할 수 있다.
- API에서 받은 책이 DB에 없으면 서버가 ISBN 상세를 조회해 BookCatalogStore로 저장한다. 제목·표지 등을 클라이언트가 임의로 저장하지 않는다.
- ISBN10과 ISBN13을 정규화한다. 이미 저장된 책은 키가 없어도 재사용하므로 중복 수집하지 않는다.
- devlop의 Book.genres / Genre.book 다중 장르 구조를 유지한다. 기존 독후감 도서 선택도 BookSelectionService를 함께 사용한다.
- 외부 HTTP 호출 뒤에 짧은 트랜잭션으로 사용자 연결을 갱신한다. API 오류·ISBN 오류 시 기존 인생 책은 보존한다.
- 해제나 독후감 삭제로 공유 Book을 삭제하지 않는다. 기존 articleService.delete의 Book 삭제를 제거했다.
- 대표 장르 하나는 기존 BookResponseDto 형식으로 반환하며 저장된 다중 장르는 유지한다.
- 아직 다른 사용자와의 추천 가중치 변경이나 인생 책 선택 이력 저장은 추가하지 않았다.

## 함께 수정한 오류

- GlobalExceptionHandler가 ResponseStatusException을 500으로 바꾸던 문제를 수정해 403·404·503 등을 보존한다.
- 인생 책·독후감 경로의 외부 도서 API 장애도 503으로 반환한다.
- 장르 구조 변경 후 삭제된 ensureGenres를 호출하던 GenreInitializer를 제거했다. 선택용 13개 장르는 taxonomy에서 제공한다.
- RecommendationStartupTest를 현재 JWT·401·JSON 로그인·문서 공개 계약으로 갱신하고 H2의 SECOND 설정도 적용했다.
- 중복 OpenAPI Bean을 제거해 공통 SwaggerConfig의 Bearer JWT 설명을 사용한다. 도서 문서 그룹에 인생 책 경로를 포함했다.

## DB 적용

개발 환경의 JPA_DDL_AUTO=update는 users.life_book_id와 Book 외래키를 추가한다. 기존 life_book_id 값은 없으므로 이전 사용자는 미설정(null) 상태로 시작한다. Book 테이블과 기존 장르 구조를 새로 만들거나 삭제하지 않는다.
운영에서 ddl-auto를 validate/none으로 사용한다면 해당 nullable 컬럼과 book(id) 외래키를 별도 DB 마이그레이션으로 반영해야 한다. 실제 사용자 DB나 .env는 이번 작업에서 수정하지 않았다.

## Swagger

/swagger-ui/index.html에서 도서 · 장르 · 추천 · 인생 책 그룹을 선택한다. Authorize에 Bearer accessToken을 입력한다.
새 인생 책 API의 요청·성공·실패 동작을 주석과 어노테이션으로 설명했다. 로그인 쿠키는 JWT 인증 대신 사용할 수 없다.

## 검증 범위

전체 Gradle 테스트와 실행 JAR 생성을 확인한다. 테스트는 H2·외부 응답 대역 및 기존 모의 HTTP 서버를 사용하며 운영 DB·실제 카카오/정보나루를 호출하지 않는다.
