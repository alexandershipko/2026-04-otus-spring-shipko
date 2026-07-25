package ru.otus.hw.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.BookComment;

public interface BookCommentRepository extends R2dbcRepository<BookComment, Long> {

    Flux<BookComment> findAllByBookId(long bookId);

    Mono<BookComment> findByIdAndBookId(long id, long bookId);

}
