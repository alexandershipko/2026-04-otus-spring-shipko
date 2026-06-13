package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.logging.TrackTime;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcOperations namedParameterJdbcOperations;

    private final GenreRepository genreRepository;

    @Override
    public Optional<Book> findById(long id) {
        Map<String, Object> params = Collections.singletonMap("id", id);

        Book book = namedParameterJdbcOperations
                .query("""
                                select b.id as book_id, b.title as book_title,
                                       a.id as author_id, a.full_name as author_full_name,
                                       g.id as genre_id, g.name as genre_name
                                from books b
                                left join authors a on b.author_id = a.id
                                left join books_genres bg on b.id = bg.book_id
                                left join genres g on bg.genre_id = g.id
                                where b.id = :id
                                """,
                        params,
                        new BookResultSetExtractor());

        return Optional.ofNullable(book);
    }

    @Override
    @TrackTime
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var books = getAllBooksWithoutGenres();
        var relations = getAllGenreRelations();

        mergeBooksInfo(books, genres, relations);

        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }

        return update(book);
    }

    @Override
    public void deleteById(long id) {
        Map<String, Object> params = Collections.singletonMap("id", id);

        namedParameterJdbcOperations.update("delete from books where id = :id", params);
    }

    private List<Book> getAllBooksWithoutGenres() {
        return namedParameterJdbcOperations.getJdbcOperations()
                .query("""
                                select b.id as book_id, b.title as book_title,
                                       a.id as author_id, a.full_name as author_full_name
                                from books b left join authors a on b.author_id = a.id
                                """,
                        new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        return namedParameterJdbcOperations.getJdbcOperations().query(
                "select book_id, genre_id from books_genres",
                (rs, rowNum) -> new BookGenreRelation(
                        rs.getLong("book_id"),
                        rs.getLong("genre_id")));
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        Map<Long, Genre> genreById = genres.stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity()));

        Map<Long, List<Genre>> genresByBookId = relations.stream()
                .collect(Collectors.groupingBy(
                        BookGenreRelation::bookId,
                        Collectors.mapping(r -> genreById.get(r.genreId()), Collectors.toList())));

        booksWithoutGenres.forEach(book ->
                book.setGenres(genresByBookId.getOrDefault(book.getId(), new ArrayList<>())));
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();

        var params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("author_id", book.getAuthor().getId());

        namedParameterJdbcOperations.update(
                "insert into books (title, author_id) values (:title, :author_id)",
                params, keyHolder, new String[]{"id"});

        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private Book update(Book book) {
        var params = new MapSqlParameterSource()
                .addValue("id", book.getId())
                .addValue("title", book.getTitle())
                .addValue("author_id", book.getAuthor().getId());

        int updatedRows = namedParameterJdbcOperations.update(
                "update books set title = :title, author_id = :author_id where id = :id", params);

        if (updatedRows == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        SqlParameterSource[] batchArgs = book.getGenres().stream()
                .map(genre -> new MapSqlParameterSource()
                        .addValue("bookId", book.getId())
                        .addValue("genreId", genre.getId()))
                .toArray(SqlParameterSource[]::new);

        namedParameterJdbcOperations.batchUpdate(
                "insert into books_genres (book_id, genre_id) values (:bookId, :genreId)", batchArgs);
    }

    private void removeGenresRelationsFor(Book book) {
        Map<String, Object> params = Collections.singletonMap("bookId", book.getId());

        namedParameterJdbcOperations.update("delete from books_genres where book_id = :bookId", params);
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("book_id");
            String title = rs.getString("book_title");

            Author author = new Author(
                    rs.getLong("author_id"),
                    rs.getString("author_full_name"));

            return new Book(id, title, author, new ArrayList<>());
        }
    }

    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book book = null;
            List<Genre> genres = new ArrayList<>();

            while (rs.next()) {
                if (book == null) {
                    Author author = new Author(
                            rs.getLong("author_id"),
                            rs.getString("author_full_name"));

                    book = new Book(
                            rs.getLong("book_id"),
                            rs.getString("book_title"),
                            author, genres);
                }

                genres.add(new Genre(
                        rs.getLong("genre_id"),
                        rs.getString("genre_name")));
            }

            return book;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }

}
