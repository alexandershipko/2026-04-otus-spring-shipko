package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.BookComment;

public interface BookCommentRepository extends ReactiveMongoRepository<BookComment, String> {

    Flux<BookComment> findAllByBookId(String bookId);

    Mono<BookComment> findByIdAndBookId(String id, String bookId);

    Mono<Void> deleteAllByBookId(String bookId);

}
