package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Mongo для работы с книгами")
@DataMongoTest
class BookRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository repository;

    private List<Author> dbAuthors;

    private List<Genre> dbGenres;

    private List<Book> dbBooks;

    @BeforeEach
    void setUp() {
        repository.deleteAll().block();
        authorRepository.deleteAll().block();
        genreRepository.deleteAll().block();

        dbAuthors = authorRepository.saveAll(IntStream.range(1, 4).boxed()
                        .map(id -> new Author(null, "Author_" + id))
                        .toList())
                .collectList()
                .block();

        dbGenres = genreRepository.saveAll(IntStream.range(1, 7).boxed()
                        .map(id -> new Genre(null, "Genre_" + id))
                        .toList())
                .collectList()
                .block();

        dbBooks = repository.saveAll(IntStream.range(1, 4).boxed()
                        .map(id -> new Book(null,
                                "BookTitle_" + id,
                                dbAuthors.get(id - 1),
                                dbGenres.subList((id - 1) * 2, (id - 1) * 2 + 2)))
                        .toList())
                .collectList()
                .block();
    }

    private static Stream<Integer> bookIndexes() {
        return Stream.of(0, 1, 2);
    }

    @DisplayName("должен загружать книгу по id")
    @ParameterizedTest
    @MethodSource("bookIndexes")
    void shouldReturnCorrectBookById(int index) {
        var expectedBook = dbBooks.get(index);

        var actualBook = repository.findById(expectedBook.getId()).block();

        assertThat(actualBook)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен загружать список всех книг")
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = repository.findAll().collectList().block();

        assertThat(actualBooks)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(dbBooks);
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldSaveNewBook() {
        var expectedBook = new Book(null, "BookTitle_10500", dbAuthors.get(0),
                List.of(dbGenres.get(0), dbGenres.get(2)));

        var returnedBook = repository.save(expectedBook).block();

        assertThat(returnedBook.getId()).isNotNull();

        var foundBook = repository.findById(returnedBook.getId()).block();

        assertThat(foundBook)
                .usingRecursiveComparison()
                .isEqualTo(returnedBook);
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    void shouldSaveUpdatedBook() {
        var bookId = dbBooks.get(0).getId();
        var expectedBook = new Book(bookId, "BookTitle_10500", dbAuthors.get(2),
                List.of(dbGenres.get(4), dbGenres.get(5)));

        assertThat(repository.findById(bookId).block())
                .usingRecursiveComparison()
                .isNotEqualTo(expectedBook);

        repository.save(expectedBook).block();

        var foundBook = repository.findById(bookId).block();

        assertThat(foundBook)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBook() {
        var bookId = dbBooks.get(0).getId();

        assertThat(repository.findById(bookId).block()).isNotNull();

        repository.deleteById(bookId).block();

        assertThat(repository.findById(bookId).block()).isNull();
    }

}
