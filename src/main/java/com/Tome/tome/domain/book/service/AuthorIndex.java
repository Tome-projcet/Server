package com.Tome.tome.domain.book.service;

import com.Tome.tome.domain.book.entity.Author;
import com.Tome.tome.domain.book.infra.ExternalBook;
import com.Tome.tome.domain.book.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthorIndex {
    private final AuthorRepository authorRepository;

    @Transactional
    public void record(List<ExternalBook> list){
        Set<String> names = new LinkedHashSet<>();
        for (ExternalBook book : list){
            if (book.writer() == null) continue;
            for(String name : book.writer().split(",")){
                String trimmed = name.trim();
                if(!trimmed.isBlank() && trimmed.length() <= 200) names.add(trimmed);
            }
        }
        if(names.isEmpty()) return;
        authorRepository.findByNameIn(names).forEach(saved -> names.remove(saved.getName()));
        names.forEach(name -> authorRepository.save(new Author(name)));
    }

    @Transactional(readOnly = true)
    public List<String> search(String keyword, int size){
        return authorRepository.findByNameContainingOrderByNameAsc(keyword, PageRequest.of(0, size))
                .stream().map(Author::getName).toList();
    }
}
