package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Mongo для работы с жанрами")
@DataMongoTest
class GenreRepositoryTest {

    @Autowired
    private GenreRepository repository;

    private List<Genre> dbGenres;

    @BeforeEach
    void setUp() {
        repository.deleteAll().block();

        dbGenres = repository.saveAll(IntStream.range(1, 7).boxed()
                        .map(id -> new Genre(null, "Genre_" + id))
                        .toList())
                .collectList()
                .block();
    }

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var actualGenres = repository.findAll().collectList().block();

        assertThat(actualGenres)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(dbGenres);
    }

    @DisplayName("должен загружать жанры по набору id")
    @Test
    void shouldReturnCorrectGenresByIds() {
        var expectedGenres = dbGenres.subList(0, 3);
        var ids = expectedGenres.stream().map(Genre::getId).collect(Collectors.toSet());

        var actualGenres = repository.findAllById(ids).collectList().block();

        assertThat(actualGenres)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedGenres);
    }

    @DisplayName("должен возвращать пустой список для несуществующих id")
    @Test
    void shouldReturnEmptyListForNonExistingIds() {
        var actualGenres = repository
                .findAllById(Set.of("000000000000000000000000", "000000000000000000000001"))
                .collectList()
                .block();

        assertThat(actualGenres).isEmpty();
    }

}
