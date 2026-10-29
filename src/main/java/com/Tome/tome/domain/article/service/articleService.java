package com.Tome.tome.domain.article.service;

import com.Tome.tome.domain.article.dto.request.AddArticleRequest;
import com.Tome.tome.domain.article.dto.request.UpdateArticleRequest;
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
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.global.exception.ForbiddenException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    @Override
    public Articles save(AddArticleRequest request, User user){
        if(request.getBookreport() == null || request.getBookreport().isBlank()){
            throw new IllegalArgumentException("독후감 내용을 입력해주세요");
        }

        Book book = findOrSaveBook(request.getIsbn());
        Articles article = Articles.builder()
                .bookreport(request.getBookreport()).user(user).book(book).build();
        return articlesRepository.save(article);
    }

    @Override
    public List<Articles> findAll(int page, int size){
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
        Articles articles = articlesRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("독후감을 찾을 수 없습니다"));
        if(articleGoodRepository.findByUserAndArticles(user, articles).isPresent()){
            articles.downGood();
            articleGoodRepository.deleteByArticlesAndUser(articles, user);
        } else {
            ArticleGood articleGood = ArticleGood.builder()
                            .articles(articles).user(user).build();
            articleGoodRepository.save(articleGood);
            articles.upGood();
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
        bookCatalogStore.upsert(List.of(found));
        return bookRepository.findByIsbn13(found.isbn13())
                .orElseThrow(() -> new NoSuchElementException("책 정보를 저장하지 못했습니다"));
    }
}
