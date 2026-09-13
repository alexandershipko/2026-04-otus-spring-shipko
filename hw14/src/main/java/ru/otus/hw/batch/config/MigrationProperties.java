package ru.otus.hw.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "migration")
public record MigrationProperties(int chunkSize) {

}
