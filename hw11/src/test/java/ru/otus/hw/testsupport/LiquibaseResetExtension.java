package ru.otus.hw.testsupport;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.DriverManager;

public class LiquibaseResetExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        var environment = SpringExtension.getApplicationContext(context).getEnvironment();
        var url = environment.getRequiredProperty("spring.liquibase.url");
        var changeLog = environment.getRequiredProperty("spring.liquibase.change-log")
                .replaceFirst("^classpath:", "");

        try (var connection = DriverManager.getConnection(url)) {
            var database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            var liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database);
            liquibase.dropAll();
            liquibase.update(new Contexts(), new LabelExpression());
        }
    }

}
