package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@RequiredArgsConstructor
@ShellComponent
public class MigrationCommands {

    private final Job migrationJob;
    private final JobLauncher jobLauncher;
    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

    @ShellMethod(value = "Запустить миграцию данных из H2 в MongoDB", key = "migrate")
    public String migrate() throws Exception {
        var jobParameters = new JobParametersBuilder(jobExplorer)
                .getNextJobParameters(migrationJob)
                .toJobParameters();

        JobExecution execution = jobLauncher.run(migrationJob, jobParameters);
        return execution.toString();
    }

    @ShellMethod(value = "Перезапустить упавшую миграцию по id выполнения", key = "migrate-restart")
    public String migrateRestart(long executionId) throws Exception {
        Long newExecutionId = jobOperator.restart(executionId);
        return jobOperator.getSummary(newExecutionId);
    }

    @ShellMethod(value = "Показать последние выполнения миграции", key = "migrate-status")
    public String migrateStatus() {
        var instances = jobExplorer.getJobInstances(migrationJob.getName(), 0, 10);
        var summary = new StringBuilder();

        for (var instance : instances) {
            jobExplorer.getJobExecutions(instance).forEach(execution ->
                    summary.append(execution.getId())
                            .append(" -> ")
                            .append(execution.getStatus())
                            .append(System.lineSeparator()));
        }

        return summary.isEmpty() ? "Миграции ещё не запускались" : summary.toString();
    }

}
