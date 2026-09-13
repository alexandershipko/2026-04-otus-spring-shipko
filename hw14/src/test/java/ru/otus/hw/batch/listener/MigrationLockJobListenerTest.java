package ru.otus.hw.batch.listener;

import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MigrationLockJobListenerTest {

    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);

    private final MigrationLockJobListener listener = new MigrationLockJobListener(mongoTemplate);

    @Test
    void shouldAcquireLockAndMarkContextOnSuccess() {
        var execution = new JobExecution(7L);
        var documentCaptor = ArgumentCaptor.forClass(Document.class);
        when(mongoTemplate.insert(documentCaptor.capture(), eq("migration_locks"))).thenReturn(new Document());

        listener.beforeJob(execution);

        assertThat(execution.getExecutionContext().containsKey("migration.lock.acquired")).isTrue();
        var insertedDocument = documentCaptor.getValue();
        assertThat(insertedDocument.getString("_id")).isEqualTo("migrationJob");
        assertThat(insertedDocument.getLong("executionId")).isEqualTo(7L);
    }

    @Test
    void shouldFailAndNotMarkContextWhenLockAlreadyExists() {
        var execution = new JobExecution(8L);
        when(mongoTemplate.insert(any(Document.class), eq("migration_locks")))
                .thenThrow(new DuplicateKeyException("duplicate"));

        assertThatThrownBy(() -> listener.beforeJob(execution))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("уже выполняется");
        assertThat(execution.getExecutionContext().containsKey("migration.lock.acquired")).isFalse();
    }

    @Test
    void shouldClearAcquiredFlagOnlyAfterConfirmedDeletion() {
        var execution = acquiredExecution();
        when(mongoTemplate.remove(any(Query.class), eq("migration_locks"))).thenReturn(DeleteResult.acknowledged(1));
        listener.afterJob(execution);
        assertThat(execution.getExecutionContext().containsKey("migration.lock.acquired")).isFalse();
        verify(mongoTemplate).remove(Query.query(org.springframework.data.mongodb.core.query.Criteria
                .where("_id").is("migrationJob").and("executionId").is(1L)), "migration_locks");
    }

    @Test
    void shouldKeepAcquiredFlagWhenNoMatchingLockWasDeleted() {
        var execution = acquiredExecution();
        when(mongoTemplate.remove(any(Query.class), eq("migration_locks"))).thenReturn(DeleteResult.acknowledged(0));
        listener.afterJob(execution);
        assertThat(execution.getExecutionContext().containsKey("migration.lock.acquired")).isTrue();
    }

    @Test
    void shouldKeepAcquiredFlagWhenDeletionWasNotAcknowledged() {
        var execution = acquiredExecution();
        when(mongoTemplate.remove(any(Query.class), eq("migration_locks"))).thenReturn(DeleteResult.unacknowledged());
        assertThatCode(() -> listener.afterJob(execution)).doesNotThrowAnyException();
        assertThat(execution.getExecutionContext().containsKey("migration.lock.acquired")).isTrue();
    }

    @Test
    void shouldKeepAcquiredFlagAndCompletedStatusWhenMongoIsUnavailable() {
        var execution = acquiredExecution();
        execution.setStatus(BatchStatus.COMPLETED);
        when(mongoTemplate.remove(any(Query.class), eq("migration_locks")))
                .thenThrow(new DataAccessResourceFailureException("MongoDB недоступна"));
        assertThatCode(() -> listener.afterJob(execution)).doesNotThrowAnyException();
        assertThat(execution.getExecutionContext().containsKey("migration.lock.acquired")).isTrue();
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void shouldNotTouchLockWithoutAcquiredFlag() {
        listener.afterJob(new JobExecution(1L));
        verifyNoInteractions(mongoTemplate);
    }

    private JobExecution acquiredExecution() {
        var execution = new JobExecution(1L);
        execution.getExecutionContext().put("migration.lock.acquired", true);
        return execution;
    }
}
