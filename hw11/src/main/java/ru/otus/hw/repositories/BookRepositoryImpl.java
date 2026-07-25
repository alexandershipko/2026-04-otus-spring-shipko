package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {

    private final R2dbcEntityOperations entityOperations;

    private final DatabaseClient databaseClient;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    @Override
    public Mono<Book> findById(long id) {
        return entityOperations.selectOne(query(where("id").is(id)), Book.class)
                .flatMap(this::enrichSingle);
    }

    @Override
    public Flux<Book> findAll() {
        return entityOperations.select(Book.class).all()
                .collectList()
                .flatMapMany(this::enrichAll);
    }

    @Override
    public Mono<Book> save(Book book) {
        var persistedBook = book.getId() == 0 ? entityOperations.insert(book) : entityOperations.update(book);

        return persistedBook.flatMap(this::saveGenreLinks);
    }

    @Override
    public Mono<Void> deleteById(long id) {
        return entityOperations.delete(query(where("id").is(id)), Book.class).then();
    }

    private Mono<Book> enrichSingle(Book book) {
        return Mono.zip(authorRepository.findById(book.getAuthorId()), findGenresByBookId(book.getId()),
                (author, genres) -> {
                    book.setAuthor(author);
                    book.setGenres(genres);

                    return book;
                });
    }

    private Flux<Book> enrichAll(List<Book> books) {
        if (books.isEmpty()) {
            return Flux.empty();
        }

        var authorIds = books.stream().map(Book::getAuthorId).collect(Collectors.toSet());
        var bookIds = books.stream().map(Book::getId).collect(Collectors.toSet());

        var authorsByIdMono = authorRepository.findAllById(authorIds).collectMap(Author::getId);
        var genresByBookIdMono = findBookGenreIds(bookIds)
                .collectMultimap(BookGenreId::bookId, BookGenreId::genreId)
                .flatMap(this::loadGenresByBookId);

        return Mono.zip(authorsByIdMono, genresByBookIdMono)
                .flatMapMany(tuple -> populateBooks(books, tuple.getT1(), tuple.getT2()));
    }

    private Flux<Book> populateBooks(List<Book> books, Map<Long, Author> authorsById,
                                      Map<Long, List<Genre>> genresByBookId) {
        books.forEach(book -> {
            book.setAuthor(authorsById.get(book.getAuthorId()));
            book.setGenres(genresByBookId.getOrDefault(book.getId(), List.of()));
        });

        return Flux.fromIterable(books);
    }

    private Mono<List<Genre>> findGenresByBookId(long bookId) {
        return findBookGenreIds(Set.of(bookId))
                .map(BookGenreId::genreId)
                .collectList()
                .flatMapMany(genreRepository::findAllById)
                .collectList();
    }

    private Mono<Map<Long, List<Genre>>> loadGenresByBookId(Map<Long, Collection<Long>> genreIdsByBookId) {
        var allGenreIds = genreIdsByBookId.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return genreRepository.findAllById(allGenreIds)
                .collectMap(Genre::getId)
                .map(genresById -> genreIdsByBookId.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().stream().map(genresById::get).toList())));
    }

    private Flux<BookGenreId> findBookGenreIds(Set<Long> bookIds) {
        return databaseClient.sql("select book_id, genre_id from books_genres where book_id in (:bookIds)")
                .bind("bookIds", bookIds)
                .map((row, metadata) -> new BookGenreId(
                        row.get("book_id", Long.class),
                        row.get("genre_id", Long.class)))
                .all();
    }

    private Mono<Book> saveGenreLinks(Book book) {
        var genreIds = book.getGenres().stream().map(Genre::getId).toList();

        return databaseClient.sql("delete from books_genres where book_id = :bookId")
                .bind("bookId", book.getId())
                .fetch()
                .rowsUpdated()
                .thenMany(Flux.fromIterable(genreIds)
                        .flatMap(genreId -> databaseClient
                                .sql("insert into books_genres (book_id, genre_id) values (:bookId, :genreId)")
                                .bind("bookId", book.getId())
                                .bind("genreId", genreId)
                                .fetch()
                                .rowsUpdated()))
                .then(Mono.just(book));
    }

    private record BookGenreId(long bookId, long genreId) {

    }

}
