package com.Tome.tome.domain.article.service;

import com.Tome.tome.domain.article.dto.request.AddArticleRequest;
import com.Tome.tome.domain.article.dto.request.UpdateArticleRequest;
import com.Tome.tome.domain.article.entity.ArticleGenre;
import com.Tome.tome.domain.article.entity.ArticleGood;
import com.Tome.tome.domain.article.entity.Articles;
import com.Tome.tome.domain.article.port.in.ArticleUseCase;
import com.Tome.tome.domain.article.repository.ArticleGenreRepository;
import com.Tome.tome.domain.article.repository.ArticleGoodRepository;
import com.Tome.tome.domain.article.repository.articlesRepository;
import com.Tome.tome.domain.book.entity.Book;
import com.Tome.tome.domain.book.infra.ExternalBook;
import com.Tome.tome.domain.book.infra.Isbn;
import com.Tome.tome.domain.book.repository.BookRepository;
import com.Tome.tome.domain.book.service.BookCatalogService;
import com.Tome.tome.domain.book.service.BookCatalogStore;
import com.Tome.tome.domain.comment.repository.CommentRepository;
import com.Tome.tome.domain.genre.entity.Genre;
import com.Tome.tome.domain.genre.repository.GenreRepository;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.global.exception.ForbiddenException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class articleService implements ArticleUseCase {
    private static final int MAX_PAGE_SIZE = 100;

    private final articlesRepository articlesRepository;
    private final ArticleGenreRepository articleGenreRepository;
    private final CommentRepository commentRepository;
    private final ArticleGoodRepository articleGoodRepository;
    private final BookCatalogService bookCatalogService;
    private final BookCatalogStore bookCatalogStore;
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Articles save(AddArticleRequest request, User user){
        if(request.getBookreport() == null || request.getBookreport().isBlank()){
            throw new IllegalArgumentException("독후감 내용을 입력해주세요");
        }

        Book book = findOrSaveBook(request.getIsbn());

        return transactionTemplate.execute(status -> {
            Articles article = articlesRepository.save(Articles.builder()
                    .bookreport(request.getBookreport()).user(user).book(book).build());

            List<Genre> list = genreRepository.findByBook(book);
            for(Genre l : list){
                ArticleGenre articleGenre = ArticleGenre.builder()
                        .bookname(l.getName()).articles(article).build();
                articleGenreRepository.save(articleGenre);
            }
            return article;
        });
    }

    @Override
    public List<Articles> findAll(int page, int size){
        if(page<0 || size<1){
            throw new IllegalArgumentException("페이지 혹은 사이즈 값 에러");
        }
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "id"));
        return articlesRepository.findAll(pageRequest).getContent();
    }

    @Override
    public Articles findById(Long id){
        return articlesRepository.findById(id).orElseThrow(() -> new NoSuchElementException("독후감을 찾을 수 없습니다"));
    }

    @Override
    @Transactional
    public void upgood(Long id, User user){
        Articles articles = findById(id);
        if(articleGoodRepository.findByUserAndArticles(user, articles).isPresent()){
            articleGoodRepository.deleteByArticlesAndUser(articles, user);
            articlesRepository.decreaseGood(id);
        } else {
            ArticleGood articleGood = ArticleGood.builder()
                    .articles(articles).user(user).build();
            articleGoodRepository.save(articleGood);
            articlesRepository.increaseGood(id);
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id, User user){
        Articles articles = findById(id);
        if(!isWriter(articles, user)){
            throw new ForbiddenException("작성자만 삭제할 수 있습니다");
        }

        commentRepository.deleteByArticles(articles);
        articleGoodRepository.deleteAllByArticles(articles);
        articleGenreRepository.deleteByArticles(articles);
        articlesRepository.delete(articles);
        return true;
    }

    @Override
    @Transactional
    public Optional<Articles> update(Long id, UpdateArticleRequest request, User user){
        Articles articles = findById(id);
        if(!isWriter(articles, user)){
            throw new ForbiddenException("작성자만 수정할 수 있습니다");
        }

        if(request.getBookreport() == null || request.getBookreport().isBlank()){
            throw new IllegalArgumentException("독후감 내용을 입력해주세요");
        }
        articles.update(request.getBookreport());
        return Optional.of(articles);
    }

    @Override
    public List<ArticleGenre> findGenre(Long id){
        findById(id);
        return articleGenreRepository.findByArticles_Id(id);
    }

    private boolean isWriter(Articles articles, User user){
        return articles.getUser() != null && articles.getUser().getId().equals(user.getId());
    }

    private Book findOrSaveBook(String isbn){
        if(isbn == null || isbn.isBlank()){
            throw new IllegalArgumentException("책을 선택해주세요");
        }
        Optional<Book> saved = bookRepository.findByIsbn13(Isbn.normalize(isbn));
        if(saved.isPresent()){
            return saved.get();
        }
        ExternalBook found = bookCatalogService.details(isbn);
        try {
            bookCatalogStore.upsert(List.of(found));
        } catch (DataIntegrityViolationException e) {
            // 다른 요청이 같은 책을 먼저 저장했다. 아래에서 그 책을 다시 조회해 사용한다.
        }
        return bookRepository.findByIsbn13(found.isbn13())
                .orElseThrow(() -> new NoSuchElementException("책 정보를 저장하지 못했습니다"));
    }
}
