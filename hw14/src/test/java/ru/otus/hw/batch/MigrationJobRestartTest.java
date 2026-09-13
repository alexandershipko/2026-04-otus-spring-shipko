package ru.otus.hw.batch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.otus.hw.batch.processor.BookCommentItemProcessor;
import ru.otus.hw.models.target.BookComment;
import ru.otus.hw.repositories.target.AuthorMongoRepository;
import ru.otus.hw.repositories.target.BookCommentMongoRepository;
import ru.otus.hw.repositories.target.BookMongoRepository;
import ru.otus.hw.repositories.target.GenreMongoRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Restart упавшего job миграции")
@SpringBootTest
@SpringBatchTest
class MigrationJobRestartTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private JobExplorer jobExplorer;

    @MockitoSpyBean
    private BookCommentItemProcessor bookCommentItemProcessor;

    @Autowired
    private AuthorMongoRepository authorRepository;

    @Autowired
    private GenreMongoRepository genreRepository;

    @Autowired
    private BookMongoRepository bookRepository;

    @Autowired
    private BookCommentMongoRepository bookCommentRepository;

    @MockitoSpyBean
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

    @DisplayName("должен переносить оставшиеся данные после restart, не трогая уже смигрированные шаги")
    @Test
    void shouldCompleteRemainingStepsOnRestart() throws Exception {
        doThrow(new RuntimeException("Симулированный сбой обработки комментария"))
                .doCallRealMethod()
                .when(bookCommentItemProcessor).process(any());
        var firstExecution = jobLauncherTestUtils.launchJob();

        assertThat(firstExecution.getExitStatus().getExitCode()).isEqualTo("FAILED");
        assertThat(authorRepository.count()).isEqualTo(3);
        assertThat(genreRepository.count()).isEqualTo(6);
        assertThat(bookRepository.count()).isEqualTo(3);
        assertThat(bookCommentRepository.count()).isEqualTo(0);

        var restartedExecutionId = jobOperator.restart(firstExecution.getId());
        var restartedExecution = jobExplorer.getJobExecution(restartedExecutionId);

        Assertions.assertNotNull(restartedExecution);
        assertThat(restartedExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        assertThat(authorRepository.count()).isEqualTo(3);
        assertThat(genreRepository.count()).isEqualTo(6);
        assertThat(bookRepository.count()).isEqualTo(3);
        assertThat(bookCommentRepository.count()).isEqualTo(3);
    }

    @DisplayName("restart после MongoDB-записи до SQL commit должен повторить chunk без дубликатов")
    @Test
    void shouldRestartAfterMongoWriteBeforeSqlCommit() throws Exception {
        var failOnce = new AtomicBoolean(true);
        var writtenComments = new AtomicReference<List<BookComment>>();
        doAnswer(invocation -> {
            var bulk = spy((BulkOperations) invocation.callRealMethod());
            doAnswer(execute -> {
                var result = execute.callRealMethod();
                if (failOnce.compareAndSet(true, false)) {
                    assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
                    writtenComments.set(bookCommentRepository.findAll());
                    throw new IllegalStateException("Сбой после записи в Mongo, но до коммита SQL");
                }
                return result;
            }).when(bulk).execute();
            return bulk;
        }).when(mongoTemplate).bulkOps(any(BulkOperations.BulkMode.class), eq(BookComment.class));

        try {
            for (var i = 0; i < 12; i++) {
                jdbcTemplate.update("insert into book_comments (text, book_id) values (?, ?)",
                        "CommitFailure_" + i, i % 3 + 1);
            }
            var failed = jobLauncherTestUtils.launchJob();
            assertThat(failed.getExitStatus().getExitCode()).isEqualTo("FAILED");
            assertThat(failOnce.get()).isFalse();
            assertThat(writtenComments.get()).hasSize(10);
            assertThat(bookCommentRepository.count()).isEqualTo(10);
            var failedStep = failed.getStepExecutions().stream()
                    .filter(step -> step.getStepName().equals("bookCommentMigrationStep"))
                    .findFirst().orElseThrow();
            assertThat(failedStep.getCommitCount()).isZero();
            assertThat(failedStep.getRollbackCount()).isPositive();

            var restarted = jobExplorer.getJobExecution(jobOperator.restart(failed.getId()));
            assertThat(restarted).isNotNull();
            assertThat(restarted.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
            var restartedStep = restarted.getStepExecutions().stream()
                    .filter(step -> step.getStepName().equals("bookCommentMigrationStep"))
                    .findFirst().orElseThrow();
            assertThat(restartedStep.getReadCount()).isEqualTo(15);
            assertThat(authorRepository.count()).isEqualTo(3);
            assertThat(genreRepository.count()).isEqualTo(6);
            assertThat(bookRepository.count()).isEqualTo(3);
            var comments = bookCommentRepository.findAll();
            assertThat(comments).hasSize(15);
            assertThat(comments).extracting(BookComment::getText).doesNotHaveDuplicates();
            assertThat(comments).extracting(BookComment::getText).containsExactlyInAnyOrderElementsOf(
                    jdbcTemplate.queryForList("select text from book_comments", String.class));
            for (var original : writtenComments.get()) {
                var restored = comments.stream().filter(comment -> comment.getId().equals(original.getId()))
                        .findFirst().orElseThrow();
                assertThat(restored.getText()).isEqualTo(original.getText());
                assertThat(restored.getBook().getId()).isEqualTo(original.getBook().getId());
            }
            var bookIds = bookRepository.findAll().stream().map(ru.otus.hw.models.target.Book::getId).toList();
            comments.forEach(comment -> assertThat(bookIds).contains(comment.getBook().getId()));
        } finally {
            jdbcTemplate.update("delete from book_comments where text like 'CommitFailure_%'");
        }
    }

}
