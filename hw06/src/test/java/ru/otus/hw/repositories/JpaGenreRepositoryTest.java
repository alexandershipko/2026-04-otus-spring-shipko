package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с жанрами")
@Import(JpaGenreRepository.class)
@DataJpaTest
class JpaGenreRepositoryTest {

    @Autowired
    private JpaGenreRepository repositoryJpa;

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var expectedGenres = IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();

        var actualGenres = repositoryJpa.findAll();

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
    }

    @DisplayName("должен загружать жанры по набору id")
    @Test
    void shouldReturnCorrectGenresByIds() {
        var actualGenres = repositoryJpa.findAllByIds(Set.of(1L, 3L, 5L));

        assertThat(actualGenres).hasSize(3)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Genre_1", "Genre_3", "Genre_5");
    }

    @DisplayName("должен возвращать пустой список для несуществующих id")
    @Test
    void shouldReturnEmptyListForNonExistingIds() {
        var actualGenres = repositoryJpa.findAllByIds(Set.of(99L, 100L));

        assertThat(actualGenres).isEmpty();
    }

}
