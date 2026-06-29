package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.JpaBookCommentRepository;
import ru.otus.hw.repositories.JpaBookRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест сервиса комментариев")
@DataJpaTest
@Import({BookCommentServiceImpl.class,
        JpaBookCommentRepository.class,
        JpaBookRepository.class})
@Transactional(propagation = Propagation.NEVER)
class BookCommentServiceImplTest {

    @Autowired
    private BookCommentService bookCommentService;

    @DisplayName("должен загружать комментарий по id без LazyInitializationException")
    @Test
    void shouldFindByIdWithoutLazyInitializationException() {
        var book = new Book(1, "BookTitle_1", null, null);
        var expectedComment = new BookComment(1, "Comment_1", book);

        var comment = bookCommentService.findById(1L);

        assertThat(comment).isPresent().get()
                .usingRecursiveComparison()
                .ignoringFields("book.author", "book.genres")
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен загружать все комментарии по id книги без LazyInitializationException")
    @Test
    void shouldFindAllByBookIdWithoutLazyInitializationException() {
        var book = new Book(1, "BookTitle_1", null, null);
        var expectedComments = List.of(
                new BookComment(1, "Comment_1", book),
                new BookComment(2, "Comment_2", book)
        );

        var comments = bookCommentService.findAllByBookId(1L);

        assertThat(comments)
                .usingRecursiveComparison()
                .ignoringFields("book.author", "book.genres")
                .isEqualTo(expectedComments);
    }

}
