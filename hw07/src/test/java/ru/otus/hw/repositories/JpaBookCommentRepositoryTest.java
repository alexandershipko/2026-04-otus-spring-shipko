package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с комментариями")
@DataJpaTest
class JpaBookCommentRepositoryTest {

    @Autowired
    private TestEntityManager tem;

    @Autowired
    private BookCommentRepository repository;

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCorrectCommentById() {
        var actualComment = repository.findById(1L);
        var expectedComment = tem.find(BookComment.class, 1L);

        assertThat(actualComment).isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldReturnCorrectCommentsByBookId() {
        var actualComments = repository.findAllByBookId(1L);

        assertThat(actualComments).hasSize(2)
                .allMatch(c -> c.getText() != null && !c.getText().isEmpty());
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        var book = tem.find(Book.class, 1L);
        var expectedComment = new BookComment(0, "New comment", book);

        var returnedComment = repository.save(expectedComment);

        assertThat(returnedComment).isNotNull()
                .matches(c -> c.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedComment);

        var foundComment = tem.find(BookComment.class, returnedComment.getId());

        assertThat(foundComment).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(returnedComment);
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        var book = tem.find(Book.class, 2L);
        var expectedComment = new BookComment(1L, "Updated comment", book);

        repository.save(expectedComment);

        var foundComment = tem.find(BookComment.class, 1L);

        assertThat(foundComment).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteComment() {
        assertThat(tem.find(BookComment.class, 1L)).isNotNull();

        repository.deleteById(1L);

        assertThat(tem.find(BookComment.class, 1L)).isNull();
    }

}
