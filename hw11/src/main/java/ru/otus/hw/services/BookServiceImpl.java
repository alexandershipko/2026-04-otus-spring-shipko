package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookCommentRepository;
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

    private final BookCommentRepository bookCommentRepository;

    @Override
    public Mono<BookDto> findById(String id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("Book with id %s not found".formatted(id))))
                .map(BookServiceImpl::toBookDto);
    }

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAll()
                .map(BookServiceImpl::toBookDto);
    }

    @Override
    public Mono<BookDto> insert(BookCreateDto bookCreateDto) {
        return Mono.zip(
                        findAuthorOrThrow(bookCreateDto.getAuthorId()),
                        findGenresOrThrow(bookCreateDto.getGenreIds()))
                .flatMap(refs -> bookRepository.save(
                        new Book(null, bookCreateDto.getTitle(), refs.getT1(), refs.getT2())))
                .map(BookServiceImpl::toBookDto);
    }

    @Override
    public Mono<BookDto> update(BookUpdateDto bookUpdateDto) {
        return ensureBookExists(bookUpdateDto.getId())
                .then(Mono.zip(
                        findAuthorOrThrow(bookUpdateDto.getAuthorId()),
                        findGenresOrThrow(bookUpdateDto.getGenreIds())))
                .flatMap(refs -> bookRepository.save(
                        new Book(bookUpdateDto.getId(), bookUpdateDto.getTitle(), refs.getT1(), refs.getT2())))
                .map(BookServiceImpl::toBookDto);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return ensureBookExists(id)
                .then(bookCommentRepository.deleteAllByBookId(id))
                .then(bookRepository.deleteById(id));
    }

    private Mono<Void> ensureBookExists(String id) {
        return bookRepository.existsById(id)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(() ->
                        new EntityNotFoundException("Book with id %s not found".formatted(id))))
                .then();
    }

    private Mono<Author> findAuthorOrThrow(String authorId) {
        return authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(() ->
                        new EntityNotFoundException("Author with id %s not found".formatted(authorId))));
    }

    private Mono<List<Genre>> findGenresOrThrow(Set<String> genreIds) {
        if (isEmpty(genreIds)) {
            return Mono.error(new IllegalArgumentException("Genres ids must not be null"));
        }

        return genreRepository.findAllById(genreIds)
                .collectList()
                .flatMap(genres -> {
                    if (isEmpty(genres) || genreIds.size() != genres.size()) {
                        return Mono.error(new EntityNotFoundException(
                                "One or all genres with ids %s not found".formatted(genreIds)));
                    }

                    return Mono.just(genres);
                });
    }

    private static BookDto toBookDto(Book book) {
        var genres = book.getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .toList();

        return new BookDto(
                book.getId(),
                book.getTitle(),
                new AuthorDto(book.getAuthor().getId(), book.getAuthor().getFullName()),
                genres);
    }

}
