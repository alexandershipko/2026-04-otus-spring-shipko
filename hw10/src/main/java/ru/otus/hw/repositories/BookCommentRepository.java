package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Optional;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {

    @Override
    Optional<BookComment> findById(Long id);

    List<BookComment> findAllByBookId(long bookId);

    Optional<BookComment> findByIdAndBookId(long id, long bookId);

}
