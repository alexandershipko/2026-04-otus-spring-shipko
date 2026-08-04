package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import ru.otus.hw.models.BookComment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Mongo для работы с комментариями")
@DataMongoTest
class BookCommentRepositoryTest {

    @Autowired
    private BookCommentRepository repository;

    private List<BookComment> dbComments;

    private String bookId;

    private String otherBookId;

    @BeforeEach
    void setUp() {
        repository.deleteAll().block();

        bookId = "000000000000000000000001";
        otherBookId = "000000000000000000000002";

        dbComments = repository.saveAll(List.of(
                        new BookComment(null, "Comment_1", bookId),
                        new BookComment(null, "Comment_2", bookId),
                        new BookComment(null, "Comment_3", otherBookId)))
                .collectList()
                .block();
    }

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCorrectCommentById() {
        var expectedComment = dbComments.get(0);

        var actualComment = repository.findById(expectedComment.getId()).block();

        assertThat(actualComment)
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldReturnCorrectCommentsByBookId() {
        var actualComments = repository.findAllByBookId(bookId).collectList().block();

        assertThat(actualComments)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(dbComments.subList(0, 2));
    }

    @DisplayName("должен загружать комментарий по id и id книги")
    @Test
    void shouldReturnCorrectCommentByIdAndBookId() {
        var expectedComment = dbComments.get(0);

        var actualComment = repository.findByIdAndBookId(expectedComment.getId(), bookId).block();

        assertThat(actualComment)
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);
    }

    @DisplayName("не должен находить комментарий через чужую книгу")
    @Test
    void shouldNotReturnCommentForAnotherBookId() {
        var expectedComment = dbComments.get(0);

        var actualComment = repository.findByIdAndBookId(expectedComment.getId(), otherBookId).block();

        assertThat(actualComment).isNull();
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        var expectedComment = new BookComment(null, "New comment", bookId);

        var returnedComment = repository.save(expectedComment).block();

        assertThat(returnedComment.getId()).isNotNull();

        var foundComment = repository.findById(returnedComment.getId()).block();

        assertThat(foundComment)
                .usingRecursiveComparison()
                .isEqualTo(returnedComment);
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        var commentId = dbComments.get(0).getId();
        var expectedComment = new BookComment(commentId, "Updated comment", otherBookId);

        repository.save(expectedComment).block();

        var foundComment = repository.findById(commentId).block();

        assertThat(foundComment)
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteComment() {
        var commentId = dbComments.get(0).getId();

        assertThat(repository.findById(commentId).block()).isNotNull();

        repository.deleteById(commentId).block();

        assertThat(repository.findById(commentId).block()).isNull();
    }

    @DisplayName("должен удалять все комментарии книги по её id")
    @Test
    void shouldDeleteAllByBookId() {
        repository.deleteAllByBookId(bookId).block();

        assertThat(repository.findAllByBookId(bookId).collectList().block()).isEmpty();
        assertThat(repository.findAllByBookId(otherBookId).collectList().block()).hasSize(1);
    }

}
