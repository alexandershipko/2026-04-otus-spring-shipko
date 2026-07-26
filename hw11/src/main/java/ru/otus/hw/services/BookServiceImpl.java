package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Mono<BookDto> findById(long id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("Book with id %d not found".formatted(id))));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BookDto> findAll() {
        return bookRepository.findAll();
    }

    @Override
    @Transactional
    public Mono<BookDto> insert(BookCreateDto bookCreateDto) {
        return validateReferences(bookCreateDto.getAuthorId(), bookCreateDto.getGenreIds())
                .then(bookRepository.insert(
                        bookCreateDto.getTitle(),
                        bookCreateDto.getAuthorId(),
                        bookCreateDto.getGenreIds()))
                .flatMap(bookRepository::findById);
    }

    @Override
    @Transactional
    public Mono<BookDto> update(BookUpdateDto bookUpdateDto) {
        return ensureBookExists(bookUpdateDto.getId())
                .then(validateReferences(bookUpdateDto.getAuthorId(), bookUpdateDto.getGenreIds()))
                .then(bookRepository.update(
                        bookUpdateDto.getId(),
                        bookUpdateDto.getTitle(),
                        bookUpdateDto.getAuthorId(),
                        bookUpdateDto.getGenreIds()))
                .flatMap(updated -> updated
                        ? bookRepository.findById(bookUpdateDto.getId())
                        : Mono.error(new EntityNotFoundException(
                                "Book with id %d not found".formatted(bookUpdateDto.getId()))));
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(long id) {
        return ensureBookExists(id)
                .then(bookRepository.deleteById(id));
    }

    private Mono<Void> ensureBookExists(long id) {
        return bookRepository.existsById(id)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(() ->
                        new EntityNotFoundException("Book with id %d not found".formatted(id))))
                .then();
    }

    private Mono<Void> validateReferences(long authorId, Set<Long> genreIds) {
        return Mono.when(findAuthorOrThrow(authorId), findGenresOrThrow(genreIds));
    }

    private Mono<Author> findAuthorOrThrow(long authorId) {
        return authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(() ->
                        new EntityNotFoundException("Author with id %d not found".formatted(authorId))));
    }

    private Mono<List<Genre>> findGenresOrThrow(Set<Long> genresIds) {
        if (isEmpty(genresIds)) {
            return Mono.error(new IllegalArgumentException("Genres ids must not be null"));
        }

        return genreRepository.findAllById(genresIds)
                .collectList()
                .flatMap(genres -> {
                    if (isEmpty(genres) || genresIds.size() != genres.size()) {
                        return Mono.error(new EntityNotFoundException(
                                "One or all genres with ids %s not found".formatted(genresIds)));
                    }

                    return Mono.just(genres);
                });
    }

}
