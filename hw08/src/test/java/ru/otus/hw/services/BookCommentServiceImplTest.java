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
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Интеграционный тест сервиса комментариев")
@DataMongoTest
@Import(BookCommentServiceImpl.class)
class BookCommentServiceImplTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCommentRepository bookCommentRepository;

    @Autowired
    private BookCommentService bookCommentService;

    private Book dbBook;

    private List<BookComment> dbComments;

    @BeforeEach
    void setUp() {
        bookCommentRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        var author = authorRepository.save(new Author(null, "Author_1"));
        var genre = genreRepository.save(new Genre(null, "Genre_1"));
        dbBook = bookRepository.save(new Book(null, "BookTitle_1", author, List.of(genre)));

        dbComments = bookCommentRepository.saveAll(List.of(
                new BookComment(null, "Comment_1", dbBook.getId()),
                new BookComment(null, "Comment_2", dbBook.getId())));
    }

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldFindById() {
        var expectedComment = dbComments.get(0);

        var comment = bookCommentService.findById(expectedComment.getId());

        assertThat(comment).isPresent().get()
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldFindAllByBookId() {
        var comments = bookCommentService.findAllByBookId(dbBook.getId());

        assertThat(comments)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(dbComments);
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldInsertComment() {
        var savedComment = bookCommentService.insert("New comment", dbBook.getId());

        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getText()).isEqualTo("New comment");
        assertThat(savedComment.getBookId()).isEqualTo(dbBook.getId());
    }

    @DisplayName("должен выбрасывать исключение при вставке комментария к несуществующей книге")
    @Test
    void shouldThrowExceptionOnInsertWithUnknownBook() {
        assertThatThrownBy(() -> bookCommentService.insert("New comment", "000000000000000000000000"))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @DisplayName("должен обновлять комментарий")
    @Test
    void shouldUpdateComment() {
        var commentId = dbComments.get(0).getId();

        var updatedComment = bookCommentService.update(commentId, "Updated comment");

        assertThat(updatedComment.getText()).isEqualTo("Updated comment");
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteComment() {
        var commentId = dbComments.get(0).getId();

        bookCommentService.deleteById(commentId);

        assertThat(bookCommentService.findById(commentId)).isEmpty();
    }

}
