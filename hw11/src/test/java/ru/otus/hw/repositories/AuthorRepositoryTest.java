package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Mongo для работы с авторами")
@DataMongoTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository repository;

    private List<Author> dbAuthors;

    @BeforeEach
    void setUp() {
        repository.deleteAll().block();

        dbAuthors = repository.saveAll(IntStream.range(1, 4).boxed()
                        .map(id -> new Author(null, "Author_" + id))
                        .toList())
                .collectList()
                .block();
    }

    @DisplayName("должен загружать автора по id")
    @Test
    void shouldReturnCorrectAuthorById() {
        var expectedAuthor = dbAuthors.get(0);

        var actualAuthor = repository.findById(expectedAuthor.getId()).block();

        assertThat(actualAuthor)
                .usingRecursiveComparison()
                .isEqualTo(expectedAuthor);
    }

    @DisplayName("должен возвращать пустой Mono для несуществующего id")
    @Test
    void shouldReturnEmptyForNonExistingId() {
        var actualAuthor = repository.findById("000000000000000000000000").block();

        assertThat(actualAuthor).isNull();
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        var actualAuthors = repository.findAll().collectList().block();

        assertThat(actualAuthors)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(dbAuthors);
    }

}
