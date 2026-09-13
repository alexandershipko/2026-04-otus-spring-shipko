package ru.otus.hw.batch.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MigrationLockJobListener implements JobExecutionListener {

    private static final String COLLECTION = "migration_locks";

    private static final String LOCK_ID = "migrationJob";

    private static final String ACQUIRED_KEY = "migration.lock.acquired";

    private final MongoTemplate mongoTemplate;

    @Override
    public void beforeJob(@NonNull JobExecution execution) {
        try {
            mongoTemplate.insert(new Document("_id", LOCK_ID).append("executionId", execution.getId()), COLLECTION);
        } catch (DuplicateKeyException ex) {
            throw new IllegalStateException("Миграция в эту MongoDB уже выполняется", ex);
        }

        execution.getExecutionContext().put(ACQUIRED_KEY, true);
    }

    @Override
    public void afterJob(@NonNull JobExecution execution) {
        if (!execution.getExecutionContext().containsKey(ACQUIRED_KEY)) {
            return;
        }
        try {
            var result = mongoTemplate.remove(
                    Query.query(Criteria.where("_id").is(LOCK_ID).and("executionId").is(execution.getId())),
                    COLLECTION);
            if (!result.wasAcknowledged()) {
                log.error("Не подтверждено снятие блокировки миграции для выполнения {}. "
                        + "Проверьте коллекцию {} перед следующим запуском", execution.getId(), COLLECTION);
                return;
            }
            if (result.getDeletedCount() == 0) {
                log.warn("Блокировка миграции для выполнения {} отсутствует или принадлежит другому владельцу. "
                        + "Коллекция: {}", execution.getId(), COLLECTION);
                return;
            }
            execution.getExecutionContext().remove(ACQUIRED_KEY);
        } catch (DataAccessException ex) {
            log.error("Не удалось снять блокировку миграции для выполнения {}. "
                    + "Проверьте коллекцию {} перед следующим запуском", execution.getId(), COLLECTION, ex);
        }
    }
}
