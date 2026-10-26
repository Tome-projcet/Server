# Tome 도서 API · 장르 · 추천

main에서 보관 중인 `_to_delete/Aladin*.java`는 빌드 대상 밖에 그대로 두어 병합 충돌을 방지한다. 실행 코드에는 알라딘 의존이 없다.

이 브랜치는 알라딘 의존을 제거하고 카카오 책 검색과 도서관 정보나루를 사용한다.
카카오는 도서 검색과 표지·소개·저자·상세 링크 보완을, 정보나루는 인기 대출 도서와 KDC 분류를 담당한다.
도서 수집 결과를 DB에 저장하고 추천 요청은 DB만 조회한다. 외부 서비스 장애나 키 미설정으로 추천 요청이 실패하지 않는다.

## 설정과 실행

프로젝트 루트 `.env` 또는 실행 환경변수에 다음 이름을 설정한다. main 인증용 `JWT_SECRET_KEY`도 32바이트 이상으로 설정해야 한다. 실제 값을 문서나 Git에 저장하지 않는다. 키 값을 Git에 올리지 않는다.

```properties
KAKAO_REST_API_KEY=카카오_REST_API_키
DATA4LIBRARY_API_KEY=정보나루_인증키
BOOK_SYNC_TOKEN=운영자가_관리하는_충분히_긴_임의_토큰
```

카카오 키는 JavaScript 키나 카카오 로그인 토큰이 아니라 REST API 키를 사용한다.
기존 `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` 설정은 그대로 사용한다.
새 API 키가 없어도 서버는 시작된다. 검색과 상세 조회는 저장된 결과가 있으면 대체 응답을 반환하고, 없다면 503을 반환한다.
빈 도서 DB에서는 추천이 빈 배열이므로 먼저 인기 도서를 동기화해야 한다.

Java 25와 Gradle Wrapper로 실행한다. 테스트는 H2를 사용하며 실제 DB와 API를 호출하지 않는다.

```sh
./gradlew test
./gradlew bootRun
```

정보나루 인증키 발급: https://data4library.kr/apiUtilization
카카오 API 문서: https://developers.kakao.com/docs/ko/daum-search/dev-guide#search-book

## API 계약

모든 담당 엔드포인트는 main의 로그인 세션 또는 Bearer JWT 인증을 사용한다. 세션 쿠키로 POST·PUT 요청을 보낼 때는 XSRF-TOKEN 쿠키와 X-XSRF-TOKEN 헤더를 함께 전송한다. Bearer JWT 요청은 main의 기존 정책을 따른다.

| 메서드 | 경로 | 용도 |
|---|---|---|
| GET | `/api/books/search?query=소설&page=1&size=10` | 카카오 도서 검색. 페이지·개수 1~50, 검색어 1~200자 |
| GET | `/api/books/{isbn}` | ISBN10/13으로 두 공급자의 상세 정보 통합 |
| POST | `/api/books/sync/popular?size=50` | 최근 30일 인기 대출 도서를 저장. `X-Book-Sync-Token` 헤더 필수 |
| GET | `/api/genres` | 관심사 선택용 장르 ID·이름 목록 |
| GET | `/api/genres/preferences` | 현재 사용자의 관심 장르 조회 |
| PUT | `/api/genres/preferences` | 관심 장르 교체. 본문 `{"names":["소설","인문"]}`, 최대 5개 |
| GET | `/api/recommend/books?limit=10` | 로그인한 사용자의 추천. 최대 30권 |

기존 `/api/recommend/books?userId=...` 요청도 본인 ID일 때 계속 허용한다. 다른 사용자의 ID는 403이다.
추천 응답은 기존 `BookResponseDto` 필드를 유지하며 `description`, `source`, `loanCount`, `recommendationReason`을 추가한다.
검색·상세 응답은 `isbn13`, `title`, `writer`, `coverUrl`, `detailUrl`, `publisher`, `description`, `genre`, `loanCount`, `source`를 반환한다.
카카오 상세 URL은 구매를 보장하는 링크가 아니므로 화면에는 '도서 정보'로 표시한다. 기존 `bookbuyurl` 필드명은 호환성을 위해 유지했다.

### 수집

동기화는 `start=2026-09-01&end=2026-09-30`처럼 기간을 지정할 수 있다. 기간은 최대 366일 차이, 미래 종료일은 허용하지 않는다.
수집 개수는 1~100권이다. 정보나루 목록을 가져온 후 카카오의 ISBN 검색으로 최대 20권만 보완한다.
카카오 실패 또는 한도 초과가 발생하면 남은 보완 요청을 중단하고 정보나루 데이터로 저장하며 `warnings`로 알린다.
정보나루 자체가 실패하면 기존 DB를 변경하지 않고 503을 반환한다. 성공 응답에는 `saved`, `start`, `end`, `warnings`가 있다.
ISBN13 유일 키를 기준으로 기존 책도 갱신한다. 정보나루 도서 목록의 중복 ISBN과 잘못된 ISBN은 제외한다.
순위는 판매 베스트셀러가 아니라 선택한 기간의 도서관 대출량 기준이다.
인기 도서 재수집은 운영자가 하루 한 번 정도 실행하는 방식으로 시작한다. 예약 수집·자동 재시도는 구현하지 않았다.

## 장르와 main 호환성

`Genre`에는 13개 공통 장르를 시작 시 등록한다. 기존 장르나 사용자 기록은 삭제하지 않는다.
`Book.genre_id` 관계, Book 생성자, `UserGenre.name/count/user` 구조를 유지해 main의 온보딩·장르 카운트 코드와 호환된다.
`UserGenreRepository`는 main의 기존 메서드들을 그대로 제공한다.
새 `BookGenre` 테이블로 바꾸지 않은 이유는 현재 추천이 도서의 대표 장르 하나를 사용하며, 팀원 코드와 기존 DB를 함께 유지하기 위해서다.

공통 장르: 소설, 시/에세이, 인문, 사회, 경제/경영, 자기계발, 과학/기술, 예술, 역사/여행, 생활/건강, 언어, 종교, 기타.
`GenreTaxonomy`가 KDC 숫자 분류와 기존 이름(예: 경제경영, 인문학)을 공통 장르로 연결한다.
KDC는 도서관 분류이므로 서점 장르와 완전히 같지 않다. 특히 자기계발 같은 분야는 후속 수동 태깅이나 별도 분류 기준이 필요하다.
카카오 검색 결과에는 장르가 없어 `기타`를 반환한다. ISBN 상세 조회에서 정보나루 결과가 있으면 분류를 보완한다.

Book에는 소개·대출량·출처·갱신 시각을 추가했다. 기존 행의 null 대출량은 0으로 처리한다.
main의 `JPA_DDL_AUTO` 설정(기본 update)이 개발 DB에 추가 컬럼을 반영한다. 운영 배포 시에는 이 변경을 정식 DB 마이그레이션으로 관리해야 한다.

## 추천 기준

사용자 장르별 count를 선호 강도로 사용한다. count가 0인 기존 관심 장르도 최초 선택으로 취급한다.
후보 점수는 `선호 강도 75% + log(1 + 대출량) 정규화 25%`이다. 동일 점수에서는 ISBN으로 순서를 고정한다.
상위 관심 장르 최대 5개에서 각 60권, 기본 인기 후보 100권으로 조회 범위를 제한한다.
같은 장르는 첫 선택 단계에서 추천 개수의 40%까지 담고, 부족한 자리는 다른 분야부터 보완한다.
후보가 한 장르뿐이면 제한을 풀어 가능한 개수를 채운다. 책이 부족하면 존재하는 책만 반환한다.
ISBN 중복을 제거하고 각 추천에 관심 장르 또는 인기 대출량에 따른 이유를 붙인다.

읽은 책 제외, 관심 없음 피드백, 작가별 취향은 아직 구현하지 않았다. 팀의 독서 기록 계약이 정해지면 연결할 수 있다.

## 테스트

로컬 모의 HTTP 서버: 두 API 응답 구조, 한글·특수문자 인코딩, ISBN 정규화, KDC 분류, 상위 API 오류와 키 비노출 검증.
서비스: 카카오 보완 실패 시 정보나루 저장 지속, 정보나루 실패 시 기존 DB 보존.
H2 통합: DB 갱신·중복 방지, 기본 추천, 장르 배분, 관심사 교체, 본인 추천 권한, 입력 검증, 동기화 토큰.
실제 API 호출은 발급된 키로 별도 확인해야 한다. 테스트에 실제 키를 넣지 않는다.

실제 내장 서버 기동과 폼 로그인 후 세션 쿠키를 통한 추천·장르 HTTP 요청도 테스트한다.

## Swagger 설명과 코드 주석

서버 실행 및 기존 로그인 후 `http://localhost:8080/swagger-ui/index.html`에 접속한다.
문서 그룹에서 **도서 · 장르 · 추천**을 선택한다. 명세 JSON은 `/v3/api-docs/books-and-recommendations`이다.
문서와 실제 API는 main의 로그인 세션 또는 Bearer JWT를 사용하며, 비로그인 요청은 로그인 화면으로 이동한다.
같은 서버에 로그인한 브라우저는 세션 쿠키를 자동 전송한다. Swagger에서 로그인 쿠키를 직접 입력할 필요는 없다. Swagger의 CSRF 자동 헤더 전송을 활성화했다. 토큰 인증은 Authorize의 bearerAuth에 main의 액세스 토큰을 입력한다.

각 API의 용도·입력 범위·기본값·응답 상태는 `@Operation`, `@Parameter`, `@ApiResponse`로 설명한다.
요청·응답 필드 의미는 `@Schema`에 작성한다. 서버에서 주입하는 인증 주체 User는 입력 목록에서 숨긴다.
실제 API 키나 토큰은 예시·주석·명세에 포함하지 않는다. 동기화 요청에서만 운영용 토큰을 헤더에 전송한다.

내부 API 클라이언트·장르 변환·ISBN 검사·DB 저장·추천 알고리즘·저장소에는 한국어 JavaDoc과 처리 이유 주석을 남겼다.
일반 JavaDoc은 IntelliJ 코드 설명용이며, 서비스 내부 함수 전체가 Swagger의 호출 가능한 API로 공개되는 것은 아니다.

## 2026-10-26 main 구조 정렬

기준 main: `094ff1b` (10월 25일). Git 병합 없이 별도 소스 검증본을 만들었다.

- 도서 수집·추천: `domain.book`의 controller/service/entity/repository/dto/infra.
- 장르 목록·관심사: `domain.genre`의 controller/service/config.
- 사용자 엔티티·저장소: main의 `domain.user`를 사용한다. 팔로우 갱신 쿼리를 유지하고 관심사 교체용 행 잠금만 추가했다.
- 공통 인증·예외: main의 `global.config`와 `global.exception`을 사용한다.
- 추천 컨트롤러는 main의 `RecommendationUseCase`를 의존한다. 기존 메서드와 개수 지정 메서드를 함께 지원한다.
- main의 JWT 0.12.6·로그인 성공 핸들러·CSRF 필터를 그대로 유지한다. 구형 JWT 클래스와 중복 엔티티는 함께 등록하지 않는다.
- 담당 API의 ProblemDetail 오류 처리는 scoped advice에 우선순위를 지정해 main의 전역 핸들러와 역할을 구분한다.
- 테스트에서 사용한 JWT 키는 가상 키이며 `.env`나 사용자의 실제 DB에는 접근하지 않았다.
