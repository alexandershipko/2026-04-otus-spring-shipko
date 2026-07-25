package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("Book with id %d not found".formatted(id))))
                .map(BookServiceImpl::toBookDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BookDto> findAll() {
        return bookRepository.findAll()
                .map(BookServiceImpl::toBookDto);
    }

    @Override
    @Transactional
    public Mono<BookDto> insert(BookCreateDto bookCreateDto) {
        return Mono.zip(findAuthorOrThrow(bookCreateDto.getAuthorId()), findGenresOrThrow(bookCreateDto.getGenreIds()),
                        (author, genres) -> new Book(0, bookCreateDto.getTitle(), author.getId(), author, genres))
                .flatMap(bookRepository::save)
                .map(BookServiceImpl::toBookDto);
    }

    @Override
    @Transactional
    public Mono<BookDto> update(BookUpdateDto bookUpdateDto) {
        return bookRepository.findById(bookUpdateDto.getId())
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookUpdateDto.getId()))))
                .then(Mono.zip(findAuthorOrThrow(bookUpdateDto.getAuthorId()),
                        findGenresOrThrow(bookUpdateDto.getGenreIds()),
                        (author, genres) -> new Book(bookUpdateDto.getId(), bookUpdateDto.getTitle(),
                                author.getId(), author, genres)))
                .flatMap(bookRepository::save)
                .map(BookServiceImpl::toBookDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(long id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("Book with id %d not found".formatted(id))))
                .then(bookRepository.deleteById(id));
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

        return genreRepository.findAllByIds(genresIds)
                .collectList()
                .flatMap(genres -> {
                    if (isEmpty(genres) || genresIds.size() != genres.size()) {
                        return Mono.error(new EntityNotFoundException(
                                "One or all genres with ids %s not found".formatted(genresIds)));
                    }

                    return Mono.just(genres);
                });
    }

    private static BookDto toBookDto(Book book) {
        var genres = book.getGenres().stream()
                .map(BookServiceImpl::toGenreDto)
                .toList();

        return new BookDto(book.getId(), book.getTitle(), toAuthorDto(book.getAuthor()), genres);
    }

    private static AuthorDto toAuthorDto(Author author) {
        return new AuthorDto(author.getId(), author.getFullName());
    }

    private static GenreDto toGenreDto(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }

}
