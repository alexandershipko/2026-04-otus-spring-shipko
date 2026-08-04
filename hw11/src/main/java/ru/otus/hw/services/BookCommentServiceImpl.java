package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCommentCreateDto;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookCommentUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;

@RequiredArgsConstructor
@Service
public class BookCommentServiceImpl implements BookCommentService {

    private final BookCommentRepository bookCommentRepository;

    private final BookRepository bookRepository;

    @Override
    public Mono<BookCommentDto> findById(String id) {
        return bookCommentRepository.findById(id)
                .switchIfEmpty(Mono.error(() ->
                        new EntityNotFoundException("Comment with id %s not found".formatted(id))))
                .map(BookCommentServiceImpl::toBookCommentDto);
    }

    @Override
    public Flux<BookCommentDto> findAllByBookId(String bookId) {
        return bookCommentRepository.findAllByBookId(bookId)
                .map(BookCommentServiceImpl::toBookCommentDto);
    }

    @Override
    public Mono<BookCommentDto> insert(BookCommentCreateDto bookCommentCreateDto) {
        return bookRepository.existsById(bookCommentCreateDto.getBookId())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Book with id %s not found".formatted(bookCommentCreateDto.getBookId()))))
                .flatMap(bookExists -> {
                    var comment = new BookComment(
                            null,
                            bookCommentCreateDto.getText(),
                            bookCommentCreateDto.getBookId());

                    return bookCommentRepository.save(comment);
                })
                .map(BookCommentServiceImpl::toBookCommentDto);
    }

    @Override
    public Mono<BookCommentDto> update(BookCommentUpdateDto bookCommentUpdateDto) {
        return bookCommentRepository.findById(bookCommentUpdateDto.getId())
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Comment with id %s not found".formatted(bookCommentUpdateDto.getId()))))
                .flatMap(comment -> {
                    comment.setText(bookCommentUpdateDto.getText());

                    return bookCommentRepository.save(comment);
                })
                .map(BookCommentServiceImpl::toBookCommentDto);
    }

    @Override
    public Mono<Void> deleteByIdAndBookId(String id, String bookId) {
        return bookCommentRepository.findByIdAndBookId(id, bookId)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Comment with id %s not found for book with id %s".formatted(id, bookId))))
                .flatMap(bookCommentRepository::delete);
    }

    private static BookCommentDto toBookCommentDto(BookComment comment) {
        return new BookCommentDto(comment.getId(), comment.getText());
    }

}
