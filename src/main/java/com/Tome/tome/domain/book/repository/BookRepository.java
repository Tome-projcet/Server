package com.Tome.tome.domain.book.repository;

import com.Tome.tome.domain.book.entity.Book;
import com.Tome.tome.domain.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** ISBN 조회, 기존 장르 이름 검색, 후보 수를 제한한 추천 조회를 담당한다. null 대출량은 0으로 정렬한다. */
public interface BookRepository extends JpaRepository<Book, Long> {

    /** ISBN 유일 조건을 이용해 기존 저장 여부를 확인한다. */
    Optional<Book> findByIsbn13(String isbn13);

    /** 대표 장르 엔티티가 일치하는 책을 조회한다. */
    List<Book> findByGenre(Genre genre);

    /** 기존 팀원 코드와의 호환성을 위한 이름 기준 조회다. */
    List<Book> findByGenre_Name(String name);
    /** 별칭 이름 목록의 후보를 대출량 내림차순·ID 오름차순으로 제한 조회한다. */
    @org.springframework.data.jpa.repository.Query("select b from Book b where b.genre.name in :names order by coalesce(b.loanCount, 0) desc, b.id asc")
    List<Book> findByGenre_NameIn(@org.springframework.data.repository.query.Param("names") java.util.Collection<String> names,
                                 org.springframework.data.domain.Pageable pageable);
    /** 기본 추천 후보를 대출량 순으로 제한 조회한다. */
    @org.springframework.data.jpa.repository.Query("select b from Book b order by coalesce(b.loanCount, 0) desc, b.id asc")
    List<Book> findAllBy(org.springframework.data.domain.Pageable pageable);
    /** 외부 검색 실패 시 DB의 제목·저자 부분 일치 검색으로 대체한다. */
    List<Book> findByTitleContainingIgnoreCaseOrWriterContainingIgnoreCase(String title, String writer,
                                                                          org.springframework.data.domain.Pageable pageable);
    /** main의 기존 페이지 조회 계약과 지연 로딩 정책을 보존한다. */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "genre")
    List<Book> findByGenre_Name(String name, org.springframework.data.domain.Pageable pageable);
}
