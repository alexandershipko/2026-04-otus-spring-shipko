package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест сервиса жанров")
@DataMongoTest
@Import(GenreServiceImpl.class)
class GenreServiceImplTest {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private GenreService genreService;

    private List<Genre> dbGenres;

    @BeforeEach
    void setUp() {
        genreRepository.deleteAll();

        dbGenres = genreRepository.saveAll(List.of(
                new Genre(null, "Genre_1"),
                new Genre(null, "Genre_2"),
                new Genre(null, "Genre_3")));
    }

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldFindAll() {
        var genres = genreService.findAll();

        assertThat(genres)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(dbGenres);
    }

}
