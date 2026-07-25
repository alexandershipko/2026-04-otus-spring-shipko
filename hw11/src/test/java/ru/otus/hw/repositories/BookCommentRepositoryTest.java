package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.testsupport.LiquibaseResetExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@DisplayName("Репозиторий для работы с комментариями")
@DataR2dbcTest
@ExtendWith(LiquibaseResetExtension.class)
class BookCommentRepositoryTest {

    @Autowired
    private R2dbcEntityTemplate entityTemplate;

    @Autowired
    private BookCommentRepository repository;

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCorrectCommentById() {
        var expectedComment = entityTemplate.selectOne(query(where("id").is(1L)), BookComment.class).block();
        var actualComment = repository.findById(1L).block();

        assertThat(actualComment)
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldReturnCorrectCommentsByBookId() {
        var actualComments = repository.findAllByBookId(1L).collectList().block();

        assertThat(actualComments).hasSize(2)
                .allMatch(c -> c.getText() != null && !c.getText().isEmpty());
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        var expectedComment = new BookComment(0, "New comment", 1L);

        var returnedComment = repository.save(expectedComment).block();

        assertThat(returnedComment).isNotNull()
                .matches(c -> c.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedComment);

        var foundComment = entityTemplate
                .selectOne(query(where("id").is(returnedComment.getId())), BookComment.class)
                .block();

        assertThat(foundComment).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(returnedComment);
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        var expectedComment = new BookComment(1L, "Updated comment", 2L);

        repository.save(expectedComment).block();

        var foundComment = entityTemplate.selectOne(query(where("id").is(1L)), BookComment.class).block();

        assertThat(foundComment).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteComment() {
        assertThat(entityTemplate.selectOne(query(where("id").is(1L)), BookComment.class).block()).isNotNull();

        repository.deleteById(1L).block();

        assertThat(entityTemplate.selectOne(query(where("id").is(1L)), BookComment.class).block()).isNull();
    }

}
