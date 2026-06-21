package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест сервиса книг")
@SpringBootTest
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать книгу по id без LazyInitializationException")
    @Test
    void shouldFindByIdWithoutLazyInitializationException() {
        var book = bookService.findById(1L);

        assertThat(book).isPresent();

        var foundBook = book.get();

        assertThat(foundBook.getAuthor()).isNotNull();
        assertThat(foundBook.getAuthor().getFullName()).isEqualTo("Author_1");

        assertThat(foundBook.getGenres()).isNotNull().hasSize(2);
        assertThat(foundBook.getGenres().get(0).getName()).isNotEmpty();
    }

    @DisplayName("должен загружать все книги без LazyInitializationException")
    @Test
    void shouldFindAllWithoutLazyInitializationException() {
        var books = bookService.findAll();

        assertThat(books).isNotEmpty();

        books.forEach(book -> {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getAuthor().getFullName()).isNotEmpty();
            assertThat(book.getGenres()).isNotNull().isNotEmpty();
            assertThat(book.getGenres().get(0).getName()).isNotEmpty();
        });
    }

}
