package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;

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
    private BookRepository bookRepository;

    @Autowired
    private BookCommentRepository bookCommentRepository;

    @Autowired
    private BookCommentService bookCommentService;

    private List<Book> dbBooks;

    private List<BookComment> dbComments;

    @BeforeEach
    void setUp() {
        bookCommentRepository.deleteAll().block();
        bookRepository.deleteAll().block();
        authorRepository.deleteAll().block();

        var author = authorRepository.save(new Author(null, "Author_1")).block();

        dbBooks = bookRepository.saveAll(List.of(
                        new Book(null, "BookTitle_1", author, List.of()),
                        new Book(null, "BookTitle_2", author, List.of())))
                .collectList()
                .block();

        dbComments = bookCommentRepository.saveAll(List.of(
                        new BookComment(null, "Comment_1", dbBooks.get(0).getId()),
                        new BookComment(null, "Comment_2", dbBooks.get(0).getId())))
                .collectList()
                .block();
    }

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldFindById() {
        var expectedComment = dbComments.get(0);

        var comment = bookCommentService.findById(expectedComment.getId()).block();

        assertThat(comment)
                .usingRecursiveComparison()
                .isEqualTo(new BookCommentDto(expectedComment.getId(), expectedComment.getText()));
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @Test
    void shouldFindAllByBookId() {
        var expectedComments = dbComments.stream()
                .map(comment -> new BookCommentDto(comment.getId(), comment.getText()))
                .toList();

        var comments = bookCommentService.findAllByBookId(dbBooks.get(0).getId()).collectList().block();

        assertThat(comments)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedComments);
    }

    @DisplayName("не должен удалять комментарий через id другой книги")
    @Test
    void shouldNotDeleteCommentThroughAnotherBook() {
        var commentId = dbComments.get(0).getId();
        var otherBookId = dbBooks.get(1).getId();

        assertThatThrownBy(() -> bookCommentService.deleteByIdAndBookId(commentId, otherBookId).block())
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(bookCommentService.findById(commentId).block()).isNotNull();
    }

}
