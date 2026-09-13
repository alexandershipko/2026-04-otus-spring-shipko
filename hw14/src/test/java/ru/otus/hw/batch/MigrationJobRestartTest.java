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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.otus.hw.batch.processor.BookCommentItemProcessor;
import ru.otus.hw.repositories.target.AuthorMongoRepository;
import ru.otus.hw.repositories.target.BookCommentMongoRepository;
import ru.otus.hw.repositories.target.BookMongoRepository;
import ru.otus.hw.repositories.target.GenreMongoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

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

    @BeforeEach
    void setUp() {
        bookCommentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();

        doThrow(new RuntimeException("Симулированный сбой обработки комментария"))
                .doCallRealMethod()
                .when(bookCommentItemProcessor).process(any());
    }

    @DisplayName("должен переносить оставшиеся данные после restart, не трогая уже смигрированные шаги")
    @Test
    void shouldCompleteRemainingStepsOnRestart() throws Exception {
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

}
