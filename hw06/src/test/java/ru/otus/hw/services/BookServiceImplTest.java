package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaGenreRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест сервиса книг")
@DataJpaTest
@Import({BookServiceImpl.class,
        JpaBookRepository.class,
        JpaAuthorRepository.class,
        JpaGenreRepository.class})
@Transactional(propagation = Propagation.NEVER)
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать книгу по id без LazyInitializationException")
    @Test
    void shouldFindByIdWithoutLazyInitializationException() {
        var expectedBook = new Book(1, "BookTitle_1",
                new Author(1, "Author_1"),
                List.of(new Genre(1, "Genre_1"), new Genre(2, "Genre_2")));

        var book = bookService.findById(1L);

        assertThat(book).isPresent().get()
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен загружать все книги без LazyInitializationException")
    @Test
    void shouldFindAllWithoutLazyInitializationException() {
        var expectedBooks = List.of(
                new Book(1, "BookTitle_1",
                        new Author(1, "Author_1"),
                        List.of(new Genre(1, "Genre_1"), new Genre(2, "Genre_2"))),
                new Book(2, "BookTitle_2",
                        new Author(2, "Author_2"),
                        List.of(new Genre(3, "Genre_3"), new Genre(4, "Genre_4"))),
                new Book(3, "BookTitle_3",
                        new Author(3, "Author_3"),
                        List.of(new Genre(5, "Genre_5"), new Genre(6, "Genre_6")))
        );

        var books = bookService.findAll();

        assertThat(books)
                .usingRecursiveComparison()
                .isEqualTo(expectedBooks);
    }

}
