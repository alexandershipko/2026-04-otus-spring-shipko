package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест сервиса авторов")
@DataMongoTest
@Import(AuthorServiceImpl.class)
class AuthorServiceImplTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorService authorService;

    private List<Author> dbAuthors;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();

        dbAuthors = authorRepository.saveAll(List.of(
                new Author(null, "Author_1"),
                new Author(null, "Author_2"),
                new Author(null, "Author_3")));
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldFindAll() {
        var authors = authorService.findAll();

        assertThat(authors)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(dbAuthors);
    }

}
