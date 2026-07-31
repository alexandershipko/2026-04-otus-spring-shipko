package ru.otus.hw.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import ru.otus.hw.migrations.repositories.MongockGenreRepository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

@ChangeUnit(id = "insert-genres", order = "002", author = "shipko")
public class InsertGenresChangeUnit {

    private static final Set<String> NAMES = Set.of(
            "Genre_1", "Genre_2", "Genre_3", "Genre_4", "Genre_5", "Genre_6");

    @Execution
    public void execution(MongockGenreRepository genreRepository) {
        genreRepository.saveAll(List.of(
                new Genre(null, "Genre_1"),
                new Genre(null, "Genre_2"),
                new Genre(null, "Genre_3"),
                new Genre(null, "Genre_4"),
                new Genre(null, "Genre_5"),
                new Genre(null, "Genre_6")
        ));
    }

    @RollbackExecution
    public void rollbackExecution(MongockGenreRepository genreRepository) {
        var genresToDelete = genreRepository.findAll().stream()
                .filter(genre -> NAMES.contains(genre.getName()))
                .toList();

        genreRepository.deleteAll(genresToDelete);
    }

}
