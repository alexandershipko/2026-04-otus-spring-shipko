package ru.otus.hw.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;

public interface BookRepository {

    Mono<Book> findById(long id);

    Flux<Book> findAll();

    Mono<Book> save(Book book);

    Mono<Void> deleteById(long id);

}
