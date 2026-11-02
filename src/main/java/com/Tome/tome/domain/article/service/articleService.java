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
import com.Tome.tome.domain.book.service.BookSelectionService;
import com.Tome.tome.domain.book.repository.BookRepository;
import com.Tome.tome.domain.comment.repository.CommentRepository;
import com.Tome.tome.domain.genre.entity.Genre;
import com.Tome.tome.domain.genre.repository.GenreRepository;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.global.exception.ForbiddenException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    private final BookSelectionService bookSelection;
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
        // 책은 인생 책·다른 독후감에서도 공유하므로 독후감 삭제로 함께 삭제하지 않는다.
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
        Long bookId = bookSelection.select(isbn);
        return bookRepository.findById(bookId).orElseThrow(() -> new NoSuchElementException("책을 찾을 수 없습니다"));
    }
}
