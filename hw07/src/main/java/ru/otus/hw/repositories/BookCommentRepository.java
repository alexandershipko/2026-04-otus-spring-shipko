package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Optional;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {

    @Override
    @EntityGraph("book-comment-book-graph")
    Optional<BookComment> findById(Long id);

    @EntityGraph("book-comment-book-graph")
    List<BookComment> findAllByBookId(long bookId);

}
