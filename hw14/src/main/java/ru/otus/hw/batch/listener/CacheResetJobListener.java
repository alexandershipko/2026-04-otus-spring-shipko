package ru.otus.hw.batch.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.lang.NonNull;
import ru.otus.hw.batch.cache.MigrationIdCache;

@Slf4j
@RequiredArgsConstructor
public class CacheResetJobListener implements JobExecutionListener {

    private final MigrationIdCache idCache;
    private final JobExplorer jobExplorer;

    @Override
    public void beforeJob(@NonNull JobExecution jobExecution) {
        var isRestart = jobExplorer.getJobExecutions(jobExecution.getJobInstance()).size() > 1;

        if (isRestart) {
            log.info("Restart обнаружен, кэш id оставлен как есть (переиспользуется из предыдущей попытки)");
            return;
        }

        log.info("Новый запуск job, очищаю кэш id");
        idCache.clear();
    }

}
