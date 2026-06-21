package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест сервиса комментариев")
@SpringBootTest
class BookCommentServiceImplTest {

    @Autowired
    private BookCommentService bookCommentService;

    @DisplayName("должен загружать комментарий по id без LazyInitializationException")
    @Test
    void shouldFindByIdWithoutLazyInitializationException() {
        var comment = bookCommentService.findById(1L);

        assertThat(comment).isPresent();
        assertThat(comment.get().getId()).isEqualTo(1L);
        assertThat(comment.get().getText()).isEqualTo("Comment_1");
    }

    @DisplayName("должен загружать все комментарии по id книги без LazyInitializationException")
    @Test
    void shouldFindAllByBookIdWithoutLazyInitializationException() {
        var comments = bookCommentService.findAllByBookId(1L);

        assertThat(comments).hasSize(2);

        comments.forEach(comment -> {
            assertThat(comment.getId()).isGreaterThan(0);
            assertThat(comment.getText()).isNotEmpty();
        });
    }

}
