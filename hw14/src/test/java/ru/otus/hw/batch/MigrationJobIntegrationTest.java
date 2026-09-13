package ru.otus.hw.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.models.target.BookComment;
import ru.otus.hw.models.target.Genre;
import ru.otus.hw.repositories.target.AuthorMongoRepository;
import ru.otus.hw.repositories.target.BookCommentMongoRepository;
import ru.otus.hw.repositories.target.BookMongoRepository;
import ru.otus.hw.repositories.target.GenreMongoRepository;

import static org.assertj.core.api.Assertions.assertThat;

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

    @BeforeEach
    void setUp() {
        bookCommentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();
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

}
