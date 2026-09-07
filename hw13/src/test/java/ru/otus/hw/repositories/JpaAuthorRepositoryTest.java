package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с авторами")
@DataJpaTest
class JpaAuthorRepositoryTest {

    @Autowired
    private TestEntityManager tem;

    @Autowired
    private AuthorRepository repository;

    @DisplayName("должен загружать автора по id")
    @Test
    void shouldReturnCorrectAuthorById() {
        var expectedAuthor = tem.find(Author.class, 1L);
        var actualAuthor = repository.findById(1L);

        assertThat(actualAuthor).isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expectedAuthor);
    }

    @DisplayName("должен возвращать пустой Optional для несуществующего id")
    @Test
    void shouldReturnEmptyForNonExistingId() {
        var actualAuthor = repository.findById(99L);

        assertThat(actualAuthor).isEmpty();
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        var expectedAuthors = IntStream.range(1, 4).boxed()
                .map(id -> new Author(id, "Author_" + id))
                .toList();

        var actualAuthors = repository.findAll();

        assertThat(actualAuthors)
                .usingRecursiveComparison()
                .isEqualTo(expectedAuthors);
    }

}