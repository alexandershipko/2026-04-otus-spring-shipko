package ru.otus.hw.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import ru.otus.hw.migrations.repositories.MongockAuthorRepository;
import ru.otus.hw.migrations.repositories.MongockBookRepository;
import ru.otus.hw.migrations.repositories.MongockGenreRepository;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ChangeUnit(id = "insert-books", order = "003", author = "shipko")
public class InsertBooksChangeUnit {

    private static final Set<String> TITLES = Set.of("BookTitle_1", "BookTitle_2", "BookTitle_3");

    @Execution
    public void execution(MongockAuthorRepository authorRepository, MongockGenreRepository genreRepository,
                           MongockBookRepository bookRepository) {
        var authorsByName = authorRepository.findAll().stream()
                .collect(Collectors.toMap(Author::getFullName, Function.identity()));
        var genresByName = genreRepository.findAll().stream()
                .collect(Collectors.toMap(Genre::getName, Function.identity()));

        bookRepository.saveAll(List.of(
                new Book(null, "BookTitle_1", authorsByName.get("Author_1"),
                        List.of(genresByName.get("Genre_1"), genresByName.get("Genre_2"))),
                new Book(null, "BookTitle_2", authorsByName.get("Author_2"),
                        List.of(genresByName.get("Genre_3"), genresByName.get("Genre_4"))),
                new Book(null, "BookTitle_3", authorsByName.get("Author_3"),
                        List.of(genresByName.get("Genre_5"), genresByName.get("Genre_6")))
        ));
    }

    @RollbackExecution
    public void rollbackExecution(MongockBookRepository bookRepository) {
        var booksToDelete = bookRepository.findAll().stream()
                .filter(book -> TITLES.contains(book.getTitle()))
                .toList();

        bookRepository.deleteAll(booksToDelete);
    }

}
