package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.testsupport.LiquibaseResetExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Query и command репозитории книг")
@DataR2dbcTest
@Import(BookRepository.class)
@ExtendWith(LiquibaseResetExtension.class)
class BookRepositoryTest {

    @Autowired
    private BookRepository repository;

    @DisplayName("должен загружать книгу по id одним read-контрактом")
    @Test
    void shouldReturnCorrectBookById() {
        var expectedBook = bookDto(1, 1, 1, 2);

        var actualBook = repository.findById(1L).block();

        assertThat(actualBook)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен потоково собирать все книги из join-строк")
    @Test
    void shouldReturnCorrectBooksList() {
        var expectedBooks = List.of(
                bookDto(1, 1, 1, 2),
                bookDto(2, 2, 3, 4),
                bookDto(3, 3, 5, 6)
        );

        var actualBooks = repository.findAll().collectList().block();

        assertThat(actualBooks)
                .usingRecursiveComparison()
                .isEqualTo(expectedBooks);
    }

    @DisplayName("должен создавать книгу через command-контракт")
    @Test
    void shouldInsertBook() {
        var bookId = repository.insert("BookTitle_10500", 1L, Set.of(1L, 3L)).block();

        assertThat(bookId).isNotNull().isPositive();
        assertThat(repository.findById(bookId).block())
                .usingRecursiveComparison()
                .isEqualTo(new BookDto(
                        bookId,
                        "BookTitle_10500",
                        new AuthorDto(1, "Author_1"),
                        List.of(new GenreDto(1, "Genre_1"), new GenreDto(3, "Genre_3"))));
    }

    @DisplayName("должен обновлять книгу и заменять связи жанров через command-контракт")
    @Test
    void shouldUpdateBook() {
        assertThat(repository.update(1L, "BookTitle_10500", 3L, Set.of(5L, 6L)).block())
                .isTrue();

        assertThat(repository.findById(1L).block())
                .usingRecursiveComparison()
                .isEqualTo(new BookDto(
                        1L,
                        "BookTitle_10500",
                        new AuthorDto(3L, "Author_3"),
                        List.of(new GenreDto(5L, "Genre_5"), new GenreDto(6L, "Genre_6"))));
    }

    @DisplayName("не должен заменять связи жанров при обновлении отсутствующей книги")
    @Test
    void shouldNotUpdateMissingBook() {
        assertThat(repository.update(99L, "Missing", 1L, Set.of(1L)).block())
                .isFalse();
    }

    @DisplayName("должен удалять книгу через command-контракт")
    @Test
    void shouldDeleteBook() {
        assertThat(repository.existsById(1L).block()).isTrue();

        repository.deleteById(1L).block();

        assertThat(repository.existsById(1L).block()).isFalse();
    }

    private static BookDto bookDto(long bookId, long authorId, long firstGenreId, long secondGenreId) {
        return new BookDto(
                bookId,
                "BookTitle_" + bookId,
                new AuthorDto(authorId, "Author_" + authorId),
                List.of(
                        new GenreDto(firstGenreId, "Genre_" + firstGenreId),
                        new GenreDto(secondGenreId, "Genre_" + secondGenreId)
                ));
    }

}
