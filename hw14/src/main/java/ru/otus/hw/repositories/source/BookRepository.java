package ru.otus.hw.repositories.source;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import ru.otus.hw.models.source.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph("book-author-genres-graph")
    @NonNull
    Page<Book> findAll(@NonNull Pageable pageable);

}
