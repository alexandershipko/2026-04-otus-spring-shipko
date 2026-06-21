package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с комментариями")
@Import(JpaBookCommentRepository.class)
@DataJpaTest
class JpaBookCommentRepositoryTest {

    @Autowired
    private TestEntityManager tem;

    @Autowired
    private JpaBookCommentRepository repositoryJpa;

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCorrectCommentById() {
        var actualComment = repositoryJpa.findById(1L);
        var expectedComment = tem.find(BookComment.class, 1L);

        assertThat(actualComment).isPresent()
                .get()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldReturnCorrectCommentsByBookId() {
        var actualComments = repositoryJpa.findAllByBookId(1L);

        assertThat(actualComments).hasSize(2)
                .allMatch(c -> c.getText() != null && !c.getText().isEmpty());
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        var book = tem.find(Book.class, 1L);
        var expectedComment = new BookComment(0, "New comment", book);

        var returnedComment = repositoryJpa.save(expectedComment);

        assertThat(returnedComment).isNotNull()
                .matches(c -> c.getId() > 0);

        tem.flush();
        tem.clear();

        var foundComment = tem.find(BookComment.class, returnedComment.getId());

        assertThat(foundComment).isNotNull()
                .matches(c -> c.getText().equals("New comment"));
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        var book = tem.find(Book.class, 2L);
        var expectedComment = new BookComment(1L, "Updated comment", book);

        repositoryJpa.save(expectedComment);

        tem.flush();
        tem.clear();

        var foundComment = tem.find(BookComment.class, 1L);

        assertThat(foundComment).isNotNull()
                .matches(c -> c.getText().equals("Updated comment"));
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteComment() {
        assertThat(tem.find(BookComment.class, 1L)).isNotNull();

        repositoryJpa.deleteById(1L);

        tem.flush();
        tem.clear();

        assertThat(tem.find(BookComment.class, 1L)).isNull();
    }

}
