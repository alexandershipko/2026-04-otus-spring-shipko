package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест сервиса книг")
@DataJpaTest
@Import(BookServiceImpl.class)
@Transactional(propagation = Propagation.NEVER)
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @MockitoBean
    private AclPermissionService aclPermissionService;

    @DisplayName("должен загружать книгу по id без LazyInitializationException")
    @Test
    void shouldFindByIdWithoutLazyInitializationException() {
        var expectedBook = new BookDto(1, "BookTitle_1",
                new AuthorDto(1, "Author_1"),
                List.of(new GenreDto(1, "Genre_1"), new GenreDto(2, "Genre_2")));

        var book = bookService.findById(1L);

        assertThat(book)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен загружать все книги без LazyInitializationException")
    @Test
    void shouldFindAllWithoutLazyInitializationException() {
        var expectedBooks = List.of(
                new BookDto(1, "BookTitle_1",
                        new AuthorDto(1, "Author_1"),
                        List.of(new GenreDto(1, "Genre_1"), new GenreDto(2, "Genre_2"))),
                new BookDto(2, "BookTitle_2",
                        new AuthorDto(2, "Author_2"),
                        List.of(new GenreDto(3, "Genre_3"), new GenreDto(4, "Genre_4"))),
                new BookDto(3, "BookTitle_3",
                        new AuthorDto(3, "Author_3"),
                        List.of(new GenreDto(5, "Genre_5"), new GenreDto(6, "Genre_6")))
        );

        var books = bookService.findAll();

        assertThat(books)
                .usingRecursiveComparison()
                .isEqualTo(expectedBooks);
    }

}
