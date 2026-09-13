package ru.otus.hw.repositories.source;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.source.BookComment;

import java.util.List;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {

    @EntityGraph(attributePaths = "book")
    List<BookComment> findByIdGreaterThanOrderByIdAsc(long id, Pageable pageable);

}
