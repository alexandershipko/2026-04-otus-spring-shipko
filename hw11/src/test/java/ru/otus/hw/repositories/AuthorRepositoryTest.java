package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import ru.otus.hw.models.Author;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@DisplayName("Репозиторий для работы с авторами")
@DataR2dbcTest
class AuthorRepositoryTest {

    @Autowired
    private R2dbcEntityTemplate entityTemplate;

    @Autowired
    private AuthorRepository repository;

    @DisplayName("должен загружать автора по id")
    @Test
    void shouldReturnCorrectAuthorById() {
        var expectedAuthor = entityTemplate.selectOne(query(where("id").is(1L)), Author.class).block();
        var actualAuthor = repository.findById(1L).block();

        assertThat(actualAuthor)
                .usingRecursiveComparison()
                .isEqualTo(expectedAuthor);
    }

    @DisplayName("должен возвращать пустой Mono для несуществующего id")
    @Test
    void shouldReturnEmptyForNonExistingId() {
        var actualAuthor = repository.findById(99L).block();

        assertThat(actualAuthor).isNull();
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        var expectedAuthors = IntStream.range(1, 4).boxed()
                .map(id -> new Author(id, "Author_" + id))
                .toList();

        var actualAuthors = repository.findAll().collectList().block();

        assertThat(actualAuthors)
                .usingRecursiveComparison()
                .isEqualTo(expectedAuthors);
    }

}
