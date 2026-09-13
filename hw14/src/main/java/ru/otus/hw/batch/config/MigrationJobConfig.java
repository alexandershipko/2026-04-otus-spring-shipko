package ru.otus.hw.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.batch.cache.MigrationIdRegistry;
import ru.otus.hw.batch.listener.CacheResetJobListener;
import ru.otus.hw.batch.listener.MigrationLockJobListener;
import ru.otus.hw.batch.processor.AuthorItemProcessor;
import ru.otus.hw.batch.processor.BookCommentItemProcessor;
import ru.otus.hw.batch.processor.BookItemProcessor;
import ru.otus.hw.batch.processor.GenreItemProcessor;
import ru.otus.hw.batch.reader.KeysetItemReader;
import ru.otus.hw.models.target.Author;
import ru.otus.hw.models.target.Book;
import ru.otus.hw.models.target.BookComment;
import ru.otus.hw.models.target.Genre;
import ru.otus.hw.repositories.source.AuthorRepository;
import ru.otus.hw.repositories.source.BookCommentRepository;
import ru.otus.hw.repositories.source.BookRepository;
import ru.otus.hw.repositories.source.GenreRepository;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToLongFunction;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MigrationProperties.class)
public class MigrationJobConfig {

    private static final String MIGRATION_JOB_NAME = "migrationJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MongoTemplate mongoTemplate;
    private final MigrationIdRegistry idRegistry;
    private final MigrationProperties migrationProperties;

    //Author
    @Bean
    @StepScope
    public KeysetItemReader<ru.otus.hw.models.source.Author> authorReader(AuthorRepository authorRepository) {
        return keysetReader("authorReader", authorRepository::findByIdGreaterThanOrderByIdAsc,
                ru.otus.hw.models.source.Author::getId);
    }

    @Bean
    public MongoItemWriter<Author> authorWriter() {
        return mongoWriter();
    }

    @Bean
    public AuthorItemProcessor authorItemProcessor() {
        return new AuthorItemProcessor(idRegistry);
    }

    @Bean
    public Step authorMigrationStep(KeysetItemReader<ru.otus.hw.models.source.Author> authorReader,
                                     AuthorItemProcessor authorItemProcessor,
                                     MongoItemWriter<Author> authorWriter) {
        return migrationStep("authorMigrationStep", authorReader, authorItemProcessor, authorWriter);
    }

    //Genre
    @Bean
    @StepScope
    public KeysetItemReader<ru.otus.hw.models.source.Genre> genreReader(GenreRepository genreRepository) {
        return keysetReader("genreReader", genreRepository::findByIdGreaterThanOrderByIdAsc,
                ru.otus.hw.models.source.Genre::getId);
    }

    @Bean
    public MongoItemWriter<Genre> genreWriter() {
        return mongoWriter();
    }

    @Bean
    public GenreItemProcessor genreItemProcessor() {
        return new GenreItemProcessor(idRegistry);
    }

    @Bean
    public Step genreMigrationStep(KeysetItemReader<ru.otus.hw.models.source.Genre> genreReader,
                                    GenreItemProcessor genreItemProcessor,
                                    MongoItemWriter<Genre> genreWriter) {
        return migrationStep("genreMigrationStep", genreReader, genreItemProcessor, genreWriter);
    }

    //Book
    @Bean
    @StepScope
    public KeysetItemReader<ru.otus.hw.models.source.Book> bookReader(BookRepository bookRepository) {
        return keysetReader("bookReader", bookRepository::findMigrationBooksAfter,
                ru.otus.hw.models.source.Book::getId);
    }

    @Bean
    public MongoItemWriter<Book> bookWriter() {
        return mongoWriter();
    }

    @Bean
    public BookItemProcessor bookItemProcessor() {
        return new BookItemProcessor(idRegistry);
    }

    @Bean
    public Step bookMigrationStep(KeysetItemReader<ru.otus.hw.models.source.Book> bookReader,
                                   BookItemProcessor bookItemProcessor,
                                   MongoItemWriter<Book> bookWriter) {
        return migrationStep("bookMigrationStep", bookReader, bookItemProcessor, bookWriter);
    }

    //BookComment
    @Bean
    @StepScope
    public KeysetItemReader<ru.otus.hw.models.source.BookComment> bookCommentReader(
            BookCommentRepository bookCommentRepository) {
        return keysetReader("bookCommentReader", bookCommentRepository::findByIdGreaterThanOrderByIdAsc,
                ru.otus.hw.models.source.BookComment::getId);
    }

    @Bean
    public MongoItemWriter<BookComment> bookCommentWriter() {
        return mongoWriter();
    }

    @Bean
    public BookCommentItemProcessor bookCommentItemProcessor() {
        return new BookCommentItemProcessor(idRegistry);
    }

    @Bean
    public Step bookCommentMigrationStep(KeysetItemReader<ru.otus.hw.models.source.BookComment> bookCommentReader,
                                          BookCommentItemProcessor bookCommentItemProcessor,
                                          MongoItemWriter<BookComment> bookCommentWriter) {
        return migrationStep("bookCommentMigrationStep", bookCommentReader, bookCommentItemProcessor,
                bookCommentWriter);
    }

    //Job
    @Bean
    public Job migrationJob(Step authorMigrationStep,
                             Step genreMigrationStep,
                             Step bookMigrationStep,
                             Step bookCommentMigrationStep,
                             MigrationLockJobListener migrationLockJobListener,
                             CacheResetJobListener cacheResetJobListener) {
        return new JobBuilder(MIGRATION_JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(migrationLockJobListener)
                .listener(cacheResetJobListener)
                .start(authorMigrationStep)
                .next(genreMigrationStep)
                .next(bookMigrationStep)
                .next(bookCommentMigrationStep)
                .build();
    }

    private <T> KeysetItemReader<T> keysetReader(String name, BiFunction<Long, Pageable, List<T>> query,
                                                  ToLongFunction<T> idExtractor) {
        return new KeysetItemReader<>(name, migrationProperties.chunkSize(),
                (lastId, pageSize) -> query.apply(lastId, PageRequest.of(0, pageSize)), idExtractor);
    }

    private <T> MongoItemWriter<T> mongoWriter() {
        return new MongoItemWriterBuilder<T>()
                .template(mongoTemplate)
                .build();
    }

    private <S, T> Step migrationStep(String name, ItemReader<S> reader, ItemProcessor<S, T> processor,
                                       ItemWriter<T> writer) {
        return new StepBuilder(name, jobRepository)
                .<S, T>chunk(migrationProperties.chunkSize(), transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

}
