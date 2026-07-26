package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(readOnly = true)
    public Mono<BookCommentDto> findById(long id) {
        return bookCommentRepository.findById(id)
                .switchIfEmpty(Mono.error(() ->
                        new EntityNotFoundException("Comment with id %d not found".formatted(id))))
                .map(BookCommentServiceImpl::toBookCommentDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BookCommentDto> findAllByBookId(long bookId) {
        return bookCommentRepository.findAllByBookId(bookId)
                .map(BookCommentServiceImpl::toBookCommentDto);
    }

    @Override
    @Transactional
    public Mono<BookCommentDto> insert(BookCommentCreateDto bookCommentCreateDto) {
        return bookRepository.existsById(bookCommentCreateDto.getBookId())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookCommentCreateDto.getBookId()))))
                .flatMap(bookExists -> {
                    var comment = new BookComment(
                            0,
                            bookCommentCreateDto.getText(),
                            bookCommentCreateDto.getBookId());

                    return bookCommentRepository.save(comment);
                })
                .map(BookCommentServiceImpl::toBookCommentDto);
    }

    @Override
    @Transactional
    public Mono<BookCommentDto> update(BookCommentUpdateDto bookCommentUpdateDto) {
        return bookCommentRepository.findById(bookCommentUpdateDto.getId())
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Comment with id %d not found".formatted(bookCommentUpdateDto.getId()))))
                .flatMap(comment -> {
                    comment.setText(bookCommentUpdateDto.getText());

                    return bookCommentRepository.save(comment);
                })
                .map(BookCommentServiceImpl::toBookCommentDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteByIdAndBookId(long id, long bookId) {
        return bookCommentRepository.findByIdAndBookId(id, bookId)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Comment with id %d not found for book with id %d".formatted(id, bookId))))
                .flatMap(bookCommentRepository::delete);
    }

    private static BookCommentDto toBookCommentDto(BookComment comment) {
        return new BookCommentDto(comment.getId(), comment.getText());
    }

}
