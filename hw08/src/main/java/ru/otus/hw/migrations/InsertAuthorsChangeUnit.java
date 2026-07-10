package ru.otus.hw.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;
import java.util.Set;

@ChangeUnit(id = "insert-authors", order = "001", author = "shipko")
public class InsertAuthorsChangeUnit {

    private static final Set<String> FULL_NAMES = Set.of("Author_1", "Author_2", "Author_3");

    @Execution
    public void execution(AuthorRepository authorRepository) {
        authorRepository.saveAll(List.of(
                new Author(null, "Author_1"),
                new Author(null, "Author_2"),
                new Author(null, "Author_3")
        ));
    }

    @RollbackExecution
    public void rollbackExecution(AuthorRepository authorRepository) {
        var authorsToDelete = authorRepository.findAll().stream()
                .filter(author -> FULL_NAMES.contains(author.getFullName()))
                .toList();

        authorRepository.deleteAll(authorsToDelete);
    }

}
