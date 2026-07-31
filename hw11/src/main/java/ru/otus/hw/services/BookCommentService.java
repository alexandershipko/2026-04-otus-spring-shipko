package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCommentCreateDto;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookCommentUpdateDto;

public interface BookCommentService {
    Mono<BookCommentDto> findById(String id);

    Flux<BookCommentDto> findAllByBookId(String bookId);

    Mono<BookCommentDto> insert(BookCommentCreateDto bookCommentCreateDto);

    Mono<BookCommentDto> update(BookCommentUpdateDto bookCommentUpdateDto);

    Mono<Void> deleteByIdAndBookId(String id, String bookId);
}
