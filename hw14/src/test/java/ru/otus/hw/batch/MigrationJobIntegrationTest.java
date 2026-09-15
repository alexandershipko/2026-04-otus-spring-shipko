package ru.otus.hw.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.otus.hw.models.target.BookComment;
import ru.otus.hw.models.target.Genre;
import ru.otus.hw.repositories.source.BookRepository;
import ru.otus.hw.repositories.target.AuthorMongoRepository;
import ru.otus.hw.repositories.target.BookCommentMongoRepository;
import ru.otus.hw.repositories.target.BookMongoRepository;
import ru.otus.hw.repositories.target.GenreMongoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Job миграции данных из H2 в MongoDB")
@SpringBootTest
@SpringBatchTest
class MigrationJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private AuthorMongoRepository authorRepository;

    @Autowired
    private GenreMongoRepository genreRepository;

    @Autowired
    private BookMongoRepository bookRepository;

    @Autowired
    private BookCommentMongoRepository bookCommentRepository;

    @Autowired
    private BookRepository sourceBookRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        bookCommentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();
        mongoTemplate.dropCollection("migration_ids");
    }

    @DisplayName("БД должна запрещать создание книги без автора и удаление связи с автором")
    @Test
    void shouldRejectBookWithoutAuthor() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into books (title, author_id) values (?, null)", "BookWithoutAuthor"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("update books set author_id = null where id = 1"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(sourceBookRepository.count()).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("select author_id from books where id = 1", Long.class))
                .isEqualTo(1L);
    }

    @DisplayName("должна читать книги страницами по возрастанию id (keyset) с загруженными связями")
    @Test
    void shouldReadBookPagesWithRelations() {
        var pageable = PageRequest.of(0, 2);
        var first = sourceBookRepository.findMigrationBooksAfter(0L, pageable);
        var second = sourceBookRepository.findMigrationBooksAfter(lastId(first), pageable);
        var empty = sourceBookRepository.findMigrationBooksAfter(lastId(second), pageable);

        assertThat(first).extracting(ru.otus.hw.models.source.Book::getId).containsExactly(1L, 2L);
        assertThat(second).extracting(ru.otus.hw.models.source.Book::getId).containsExactly(3L);
        assertThat(empty).isEmpty();
        first.forEach(book -> {
            assertThat(book.getAuthor().getFullName()).isEqualTo("Author_" + book.getId());
            assertThat(book.getGenres()).hasSize(2);
        });
        second.forEach(book -> {
            assertThat(book.getAuthor().getFullName()).isEqualTo("Author_" + book.getId());
            assertThat(book.getGenres()).hasSize(2);
        });
    }

    @DisplayName("повторный запуск должен сохранять ID документов и связи без дубликатов")
    @Test
    void shouldRepeatMigrationWithoutDuplicates() throws Exception {
        assertThat(jobLauncherTestUtils.launchJob().getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        var authors = authorRepository.findAll();
        var genres = genreRepository.findAll();
        var books = bookRepository.findAll();
        var comments = bookCommentRepository.findAll();

        assertThat(jobLauncherTestUtils.launchJob().getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        assertThat(authorRepository.findAll()).containsExactlyInAnyOrderElementsOf(authors);
        assertThat(genreRepository.findAll()).containsExactlyInAnyOrderElementsOf(genres);
        assertThat(bookRepository.findAll()).containsExactlyInAnyOrderElementsOf(books);
        assertThat(bookCommentRepository.findAll()).containsExactlyInAnyOrderElementsOf(comments);
        bookRepository.findAll().forEach(book -> {
            assertThat(authors).extracting(ru.otus.hw.models.target.Author::getId)
                    .contains(book.getAuthor().getId());
            assertThat(genres).extracting(Genre::getId)
                    .containsAll(book.getGenres().stream().map(Genre::getId).toList());
        });
        bookCommentRepository.findAll().forEach(comment -> {
            var original = comments.stream().filter(item -> item.getId().equals(comment.getId()))
                    .findFirst().orElseThrow();
            assertThat(comment.getBook().getId()).isEqualTo(original.getBook().getId());
            assertThat(books).extracting(ru.otus.hw.models.target.Book::getId).contains(comment.getBook().getId());
        });
    }

    @DisplayName("должна успешно переносить авторов, жанры, книги и комментарии с сохранением связей")
    @Test
    void shouldMigrateAllDataWithRelationsPreserved() throws Exception {
        var jobExecution = jobLauncherTestUtils.launchJob();

        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        assertThat(authorRepository.count()).isEqualTo(3);
        assertThat(genreRepository.count()).isEqualTo(6);
        assertThat(bookRepository.count()).isEqualTo(3);
        assertThat(bookCommentRepository.count()).isEqualTo(3);

        var book1 = bookRepository.findAll().stream()
                .filter(book -> book.getTitle().equals("BookTitle_1"))
                .findFirst()
                .orElseThrow();

        assertThat(book1.getAuthor().getFullName()).isEqualTo("Author_1");
        assertThat(book1.getGenres())
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Genre_1", "Genre_2");

        var commentsForBook1 = bookCommentRepository.findAll().stream()
                .filter(comment -> comment.getBook().getId().equals(book1.getId()))
                .toList();

        assertThat(commentsForBook1)
                .extracting(BookComment::getText)
                .containsExactlyInAnyOrder("Comment_1", "Comment_2");
    }

    private long lastId(java.util.List<ru.otus.hw.models.source.Book> page) {
        return page.getLast().getId();
    }

}
