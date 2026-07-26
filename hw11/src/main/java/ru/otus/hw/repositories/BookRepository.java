package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class BookRepository {

    private static final String SELECT_BOOKS = """
            select b.id as book_id,
                   b.title as book_title,
                   a.id as author_id,
                   a.full_name as author_name,
                   g.id as genre_id,
                   g.name as genre_name
            from books b
            join authors a on a.id = b.author_id
            left join books_genres bg on bg.book_id = b.id
            left join genres g on g.id = bg.genre_id
            """;

    private final R2dbcEntityTemplate template;

    public Mono<BookDto> findById(long id) {
        return template.getDatabaseClient()
                .sql(SELECT_BOOKS + " where b.id = :bookId order by g.id")
                .bind("bookId", id)
                .map(this::mapRow)
                .all()
                .collectList()
                .filter(rows -> !rows.isEmpty())
                .map(this::toBookDto);
    }

    public Flux<BookDto> findAll() {
        return template.getDatabaseClient()
                .sql(SELECT_BOOKS + " order by b.id, g.id")
                .map(this::mapRow)
                .all()
                .bufferUntilChanged(BookRow::bookId)
                .map(this::toBookDto);
    }

    public Mono<Boolean> existsById(long id) {
        return template.getDatabaseClient()
                .sql("select count(*) as book_count from books where id = :bookId")
                .bind("bookId", id)
                .map(row -> Objects.requireNonNull(row.get("book_count", Long.class)) > 0)
                .one();
    }

    public Mono<Long> insert(String title, long authorId, Set<Long> genreIds) {
        return template.insert(new Book(null, title, authorId))
                .flatMap(book -> replaceGenreLinks(book.getId(), genreIds).thenReturn(book.getId()));
    }

    public Mono<Boolean> update(long id, String title, long authorId, Set<Long> genreIds) {
        return template.update(
                        query(where("id").is(id)),
                        org.springframework.data.relational.core.query.Update.update("title", title)
                                .set("author_id", authorId),
                        Book.class)
                .flatMap(rowsUpdated -> rowsUpdated == 0
                        ? Mono.just(false)
                        : replaceGenreLinks(id, genreIds).thenReturn(true));
    }

    public Mono<Void> deleteById(long id) {
        return template.delete(query(where("id").is(id)), Book.class).then();
    }

    private Mono<Void> replaceGenreLinks(long bookId, Set<Long> genreIds) {
        return template.getDatabaseClient()
                .sql("delete from books_genres where book_id = :bookId")
                .bind("bookId", bookId)
                .fetch()
                .rowsUpdated()
                .thenMany(Flux.fromIterable(genreIds)
                        .concatMap(genreId -> insertGenreLink(bookId, genreId)))
                .then();
    }

    private Mono<Long> insertGenreLink(long bookId, long genreId) {
        return template.getDatabaseClient()
                .sql(
                        "insert into books_genres (book_id, genre_id) values (:bookId, :genreId)")
                .bind("bookId", bookId)
                .bind("genreId", genreId)
                .fetch()
                .rowsUpdated();
    }

    private BookRow mapRow(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        return new BookRow(
                Objects.requireNonNull(row.get("book_id", Long.class)),
                row.get("book_title", String.class),

                Objects.requireNonNull(row.get("author_id", Long.class)),
                row.get("author_name", String.class),
                row.get("genre_id", Long.class),
                row.get("genre_name", String.class)
        );
    }

    private BookDto toBookDto(List<BookRow> rows) {
        var first = rows.get(0);
        var genres = rows.stream()
                .filter(row -> row.genreId() != null)
                .map(row -> new GenreDto(row.genreId(), row.genreName()))
                .toList();

        return new BookDto(
                first.bookId(),
                first.bookTitle(),
                new AuthorDto(first.authorId(), first.authorName()),
                genres);
    }

    private record BookRow(
            long bookId,
            String bookTitle,
            long authorId,
            String authorName,
            Long genreId,
            String genreName) {
    }

}
