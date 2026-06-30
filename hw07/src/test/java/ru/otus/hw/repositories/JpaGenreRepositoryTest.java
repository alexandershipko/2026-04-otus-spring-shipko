package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с жанрами")
@DataJpaTest
class JpaGenreRepositoryTest {

    @Autowired
    private GenreRepository repository;

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var expectedGenres = IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();

        var actualGenres = repository.findAll();

        assertThat(actualGenres)
                .usingRecursiveComparison()
                .isEqualTo(expectedGenres);
    }

    @DisplayName("должен загружать жанры по набору id")
    @Test
    void shouldReturnCorrectGenresByIds() {
        var actualGenres = repository.findAllByIds(Set.of(1L, 3L, 5L));

        var expectedGenres = List.of(
                new Genre(1, "Genre_1"),
                new Genre(3, "Genre_3"),
                new Genre(5, "Genre_5"));

        assertThat(actualGenres)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedGenres);
    }

    @DisplayName("должен возвращать пустой список для несуществующих id")
    @Test
    void shouldReturnEmptyListForNonExistingIds() {
        var actualGenres = repository.findAllByIds(Set.of(99L, 100L));

        assertThat(actualGenres).isEmpty();
    }

}
