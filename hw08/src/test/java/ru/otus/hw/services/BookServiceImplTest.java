package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exceptions.DocumentNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Интеграционный тест сервиса книг")
@DataMongoTest
@Import(BookServiceImpl.class)
class BookServiceImplTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    private List<Author> dbAuthors;

    private List<Genre> dbGenres;

    private List<Book> dbBooks;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        dbAuthors = authorRepository.saveAll(List.of(
                new Author(null, "Author_1"),
                new Author(null, "Author_2"),
                new Author(null, "Author_3")));

        dbGenres = genreRepository.saveAll(List.of(
                new Genre(null, "Genre_1"),
                new Genre(null, "Genre_2"),
                new Genre(null, "Genre_3"),
                new Genre(null, "Genre_4")));

        dbBooks = bookRepository.saveAll(List.of(
                new Book(null, "BookTitle_1", dbAuthors.get(0), List.of(dbGenres.get(0), dbGenres.get(1))),
                new Book(null, "BookTitle_2", dbAuthors.get(1), List.of(dbGenres.get(2), dbGenres.get(3)))));
    }

    @DisplayName("должен загружать книгу по id со всеми вложенными данными")
    @Test
    void shouldFindById() {
        var expectedBook = dbBooks.get(0);

        var book = bookService.findById(expectedBook.getId());

        assertThat(book).isPresent().get()
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен загружать все книги со всеми вложенными данными")
    @Test
    void shouldFindAll() {
        var books = bookService.findAll();

        assertThat(books)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(dbBooks);
    }

    @DisplayName("должен сохранять новую книгу с автором и жанрами, скопированными на момент вставки")
    @Test
    void shouldInsertBook() {
        var author = dbAuthors.get(2);
        var genreIds = Set.of(dbGenres.get(0).getId(), dbGenres.get(2).getId());

        var savedBook = bookService.insert("BookTitle_3", author.getId(), genreIds);

        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getAuthor()).isEqualTo(author);
        assertThat(savedBook.getGenres())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(dbGenres.get(0), dbGenres.get(2));
    }

    @DisplayName("должен выбрасывать исключение при вставке книги с несуществующим автором")
    @Test
    void shouldThrowExceptionOnInsertWithUnknownAuthor() {
        var genreIds = Set.of(dbGenres.get(0).getId());

        assertThatThrownBy(() -> bookService.insert("BookTitle_3", "000000000000000000000000", genreIds))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @DisplayName("должен обновлять книгу")
    @Test
    void shouldUpdateBook() {
        var book = dbBooks.get(0);
        var newAuthor = dbAuthors.get(2);
        var newGenreIds = dbGenres.stream().skip(2).map(Genre::getId).collect(Collectors.toSet());

        var updatedBook = bookService.update(book.getId(), "Updated title", newAuthor.getId(), newGenreIds);

        assertThat(updatedBook.getTitle()).isEqualTo("Updated title");
        assertThat(updatedBook.getAuthor()).isEqualTo(newAuthor);
        assertThat(updatedBook.getGenres())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(dbGenres.subList(2, 4));
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBook() {
        var bookId = dbBooks.get(0).getId();

        bookService.deleteById(bookId);

        assertThat(bookService.findById(bookId)).isEmpty();
    }

}
