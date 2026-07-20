package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Интеграционный тест сервиса комментариев")
@DataJpaTest
@Import(BookCommentServiceImpl.class)
@Transactional(propagation = Propagation.NEVER)
class BookCommentServiceImplTest {

    @Autowired
    private BookCommentService bookCommentService;

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldFindById() {
        var expectedComment = new BookCommentDto(1, "Comment_1");

        var comment = bookCommentService.findById(1L);

        assertThat(comment)
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldFindAllByBookId() {
        var expectedComments = List.of(
                new BookCommentDto(1, "Comment_1"),
                new BookCommentDto(2, "Comment_2")
        );

        var comments = bookCommentService.findAllByBookId(1L);

        assertThat(comments)
                .usingRecursiveComparison()
                .isEqualTo(expectedComments);
    }

    @DisplayName("не должен удалять комментарий через id другой книги")
    @Test
    void shouldNotDeleteCommentThroughAnotherBook() {
        assertThatThrownBy(() -> bookCommentService.deleteByIdAndBookId(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(bookCommentService.findById(1L)).isNotNull();
    }

}
