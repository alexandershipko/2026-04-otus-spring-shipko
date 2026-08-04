package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookCommentRepository;
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
    private BookCommentRepository bookCommentRepository;

    @Autowired
    private BookService bookService;

    private List<Author> dbAuthors;

    private List<Genre> dbGenres;

    private List<Book> dbBooks;

    @BeforeEach
    void setUp() {
        bookCommentRepository.deleteAll().block();
        bookRepository.deleteAll().block();
        authorRepository.deleteAll().block();
        genreRepository.deleteAll().block();

        dbAuthors = authorRepository.saveAll(List.of(
                        new Author(null, "Author_1"),
                        new Author(null, "Author_2"),
                        new Author(null, "Author_3")))
                .collectList()
                .block();

        dbGenres = genreRepository.saveAll(List.of(
                        new Genre(null, "Genre_1"),
                        new Genre(null, "Genre_2"),
                        new Genre(null, "Genre_3"),
                        new Genre(null, "Genre_4")))
                .collectList()
                .block();

        dbBooks = bookRepository.saveAll(List.of(
                        new Book(null, "BookTitle_1", dbAuthors.get(0), List.of(dbGenres.get(0), dbGenres.get(1))),
                        new Book(null, "BookTitle_2", dbAuthors.get(1), List.of(dbGenres.get(2), dbGenres.get(3)))))
                .collectList()
                .block();

        bookCommentRepository.saveAll(List.of(
                        new BookComment(null, "Comment_1", dbBooks.get(0).getId()),
                        new BookComment(null, "Comment_2", dbBooks.get(0).getId())))
                .collectList()
                .block();
    }

    @DisplayName("должен загружать книгу по id со всеми вложенными данными")
    @Test
    void shouldFindById() {
        var expectedBook = dbBooks.get(0);

        var book = bookService.findById(expectedBook.getId()).block();

        assertThat(book)
                .usingRecursiveComparison()
                .isEqualTo(toBookDto(expectedBook));
    }

    @DisplayName("должен загружать все книги со всеми вложенными данными без N+1")
    @Test
    void shouldFindAll() {
        var expectedBooks = dbBooks.stream().map(BookServiceImplTest::toBookDto).toList();

        var books = bookService.findAll().collectList().block();

        assertThat(books)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedBooks);
    }

    @DisplayName("должен сохранять новую книгу с автором и жанрами, скопированными на момент вставки")
    @Test
    void shouldInsertBook() {
        var author = dbAuthors.get(2);
        var genreIds = Set.of(dbGenres.get(0).getId(), dbGenres.get(2).getId());

        var savedBook = bookService.insert(new BookCreateDto("BookTitle_3", author.getId(), genreIds)).block();

        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getAuthor())
                .usingRecursiveComparison()
                .isEqualTo(new AuthorDto(author.getId(), author.getFullName()));
        assertThat(savedBook.getGenres())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new GenreDto(dbGenres.get(0).getId(), dbGenres.get(0).getName()),
                        new GenreDto(dbGenres.get(2).getId(), dbGenres.get(2).getName()));
    }

    @DisplayName("должен выбрасывать исключение при вставке книги с несуществующим автором")
    @Test
    void shouldThrowExceptionOnInsertWithUnknownAuthor() {
        var genreIds = Set.of(dbGenres.get(0).getId());

        assertThatThrownBy(() -> bookService.insert(
                new BookCreateDto("BookTitle_3", "000000000000000000000000", genreIds)).block())
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("должен обновлять книгу")
    @Test
    void shouldUpdateBook() {
        var book = dbBooks.get(0);
        var newAuthor = dbAuthors.get(2);
        var newGenreIds = dbGenres.stream().skip(2).map(Genre::getId).collect(Collectors.toSet());

        var updatedBook = bookService.update(
                new BookUpdateDto(book.getId(), "Updated title", newAuthor.getId(), newGenreIds)).block();

        assertThat(updatedBook.getTitle()).isEqualTo("Updated title");
        assertThat(updatedBook.getAuthor())
                .usingRecursiveComparison()
                .isEqualTo(new AuthorDto(newAuthor.getId(), newAuthor.getFullName()));
        assertThat(updatedBook.getGenres())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(dbGenres.subList(2, 4).stream()
                        .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                        .toList());
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBook() {
        var bookId = dbBooks.get(0).getId();

        bookService.deleteById(bookId).block();

        assertThatThrownBy(() -> bookService.findById(bookId).block())
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("должен удалять комментарии книги вместе с ней, не оставляя несогласованных данных")
    @Test
    void shouldDeleteBookCommentsOnBookDeletion() {
        var bookId = dbBooks.get(0).getId();

        assertThat(bookCommentRepository.findAllByBookId(bookId).collectList().block()).isNotEmpty();

        bookService.deleteById(bookId).block();

        assertThat(bookCommentRepository.findAllByBookId(bookId).collectList().block()).isEmpty();
    }

    private static BookDto toBookDto(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                new AuthorDto(book.getAuthor().getId(), book.getAuthor().getFullName()),
                book.getGenres().stream()
                        .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                        .toList());
    }

}
