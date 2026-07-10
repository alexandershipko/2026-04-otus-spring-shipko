package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.listeners.BookMongoEventListener;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Mongo для работы с комментариями")
@DataMongoTest
@Import(BookMongoEventListener.class)
class BookCommentRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCommentRepository repository;

    private Book dbBook;

    private List<BookComment> dbComments;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        var author = authorRepository.save(new Author(null, "Author_1"));
        var genre = genreRepository.save(new Genre(null, "Genre_1"));
        dbBook = bookRepository.save(new Book(null, "BookTitle_1", author, List.of(genre)));

        dbComments = repository.saveAll(List.of(
                new BookComment(null, "Comment_1", dbBook),
                new BookComment(null, "Comment_2", dbBook)
        ));
    }

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCorrectCommentById() {
        var expectedComment = dbComments.get(0);

        var actualComment = repository.findById(expectedComment.getId());

        assertThat(actualComment).isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("book")
                .isEqualTo(expectedComment);

        assertThat(actualComment.get().getBook().getId()).isEqualTo(expectedComment.getBook().getId());
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldReturnCorrectCommentsByBookId() {
        var actualComments = repository.findAllByBookId(dbBook.getId());

        assertThat(actualComments)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields("book")
                .isEqualTo(dbComments);

        assertThat(actualComments)
                .extracting(comment -> comment.getBook().getId())
                .containsOnly(dbBook.getId());
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        var expectedComment = new BookComment(null, "New comment", dbBook);

        var returnedComment = repository.save(expectedComment);

        assertThat(returnedComment.getId()).isNotNull();

        var foundComment = repository.findById(returnedComment.getId());

        assertThat(foundComment).isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("book")
                .isEqualTo(returnedComment);

        assertThat(foundComment.get().getBook().getId()).isEqualTo(returnedComment.getBook().getId());
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        var commentId = dbComments.get(0).getId();
        var expectedComment = new BookComment(commentId, "Updated comment", dbBook);

        repository.save(expectedComment);

        var foundComment = repository.findById(commentId);

        assertThat(foundComment).isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("book")
                .isEqualTo(expectedComment);

        assertThat(foundComment.get().getBook().getId()).isEqualTo(expectedComment.getBook().getId());
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteComment() {
        var commentId = dbComments.get(0).getId();

        assertThat(repository.findById(commentId)).isPresent();

        repository.deleteById(commentId);

        assertThat(repository.findById(commentId)).isEmpty();
    }

    @DisplayName("должен удалять все комментарии книги при удалении книги (каскад через MongoEventListener)")
    @Test
    void shouldDeleteAllCommentsWhenBookIsDeleted() {
        assertThat(repository.findAllByBookId(dbBook.getId())).hasSize(2);

        bookRepository.deleteById(dbBook.getId());

        assertThat(repository.findAllByBookId(dbBook.getId())).isEmpty();
    }

}
