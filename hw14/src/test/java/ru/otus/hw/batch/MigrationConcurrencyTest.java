package ru.otus.hw.batch;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.otus.hw.batch.processor.AuthorItemProcessor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
class MigrationConcurrencyTest {

    @Autowired
    private Job migrationJob;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockitoSpyBean
    private AuthorItemProcessor authorItemProcessor;

    @Test
    void shouldRejectConcurrentLaunchAndRestartWithoutReleasingActiveLock() throws Exception {
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();

            if (!release.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Истекло время ожидания release в тесте");
            }

            return invocation.callRealMethod();
        }).when(authorItemProcessor).process(any());

        try (var executor = Executors.newSingleThreadExecutor()) {
            try {
                var first = executor.submit(() -> jobLauncher.run(migrationJob,
                        new JobParametersBuilder().addString("testRun", "first").toJobParameters()));
                assertThat(entered.await(30, TimeUnit.SECONDS)).isTrue();
                var activeLock = mongoTemplate.findAll(Document.class, "migration_locks");
                assertThat(activeLock).hasSize(1);
                var rejected = jobLauncher.run(migrationJob,
                        new JobParametersBuilder().addString("testRun", "second").toJobParameters());
                assertThat(rejected.getStatus().name()).isEqualTo("FAILED");
                assertThat(rejected.getStepExecutions()).isEmpty();
                assertThat(rejected.getAllFailureExceptions()).anySatisfy(exception ->
                        assertThat(exception).hasMessageContaining("уже выполняется"));
                var rejectedRestart = jobExplorer.getJobExecution(jobOperator.restart(rejected.getId()));
                assertThat(rejectedRestart).isNotNull();
                assertThat(rejectedRestart.getStatus().name()).isEqualTo("FAILED");
                assertThat(rejectedRestart.getStepExecutions()).isEmpty();
                assertThat(mongoTemplate.findAll(Document.class, "migration_locks")).isEqualTo(activeLock);
                release.countDown();
                assertThat(first.get(30, TimeUnit.SECONDS).getStatus().name()).isEqualTo("COMPLETED");
                assertThat(mongoTemplate.findAll(Document.class, "migration_locks")).isEmpty();
                var restarted = jobExplorer.getJobExecution(jobOperator.restart(rejectedRestart.getId()));
                assertThat(restarted).isNotNull();
                assertThat(restarted.getStatus().name()).isEqualTo("COMPLETED");
                assertThat(mongoTemplate.findAll(Document.class, "migration_locks")).isEmpty();
            } finally {
                release.countDown();
            }
        }
    }
}
