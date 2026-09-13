package ru.otus.hw.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.batch.cache.MigrationIdCache;
import ru.otus.hw.batch.listener.CacheResetJobListener;
import ru.otus.hw.batch.processor.AuthorItemProcessor;
import ru.otus.hw.batch.processor.BookCommentItemProcessor;
import ru.otus.hw.batch.processor.BookItemProcessor;
import ru.otus.hw.batch.processor.GenreItemProcessor;
import ru.otus.hw.models.target.Author;
import ru.otus.hw.models.target.Book;
import ru.otus.hw.models.target.BookComment;
import ru.otus.hw.models.target.Genre;
import ru.otus.hw.repositories.source.AuthorRepository;
import ru.otus.hw.repositories.source.BookCommentRepository;
import ru.otus.hw.repositories.source.BookRepository;
import ru.otus.hw.repositories.source.GenreRepository;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class MigrationJobConfig {

    private static final String MIGRATION_JOB_NAME = "migrationJob";
    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MongoTemplate mongoTemplate;
    private final MigrationIdCache idCache;

    //Author
    @Bean
    public RepositoryItemReader<ru.otus.hw.models.source.Author> authorReader(AuthorRepository authorRepository) {
        return new RepositoryItemReaderBuilder<ru.otus.hw.models.source.Author>()
                .name("authorReader")
                .repository(authorRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public MongoItemWriter<Author> authorWriter() {
        return new MongoItemWriterBuilder<Author>()
                .template(mongoTemplate)
                .build();
    }

    @Bean
    public AuthorItemProcessor authorItemProcessor() {
        return new AuthorItemProcessor(idCache);
    }

    @Bean
    public Step authorMigrationStep(RepositoryItemReader<ru.otus.hw.models.source.Author> authorReader,
                                     AuthorItemProcessor authorItemProcessor,
                                     MongoItemWriter<Author> authorWriter) {
        return new StepBuilder("authorMigrationStep", jobRepository)
                .<ru.otus.hw.models.source.Author, Author>chunk(CHUNK_SIZE, transactionManager)
                .reader(authorReader)
                .processor(authorItemProcessor)
                .writer(authorWriter)
                .build();
    }

    //Genre
    @Bean
    public RepositoryItemReader<ru.otus.hw.models.source.Genre> genreReader(GenreRepository genreRepository) {
        return new RepositoryItemReaderBuilder<ru.otus.hw.models.source.Genre>()
                .name("genreReader")
                .repository(genreRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public MongoItemWriter<Genre> genreWriter() {
        return new MongoItemWriterBuilder<Genre>()
                .template(mongoTemplate)
                .build();
    }

    @Bean
    public GenreItemProcessor genreItemProcessor() {
        return new GenreItemProcessor(idCache);
    }

    @Bean
    public Step genreMigrationStep(RepositoryItemReader<ru.otus.hw.models.source.Genre> genreReader,
                                    GenreItemProcessor genreItemProcessor,
                                    MongoItemWriter<Genre> genreWriter) {
        return new StepBuilder("genreMigrationStep", jobRepository)
                .<ru.otus.hw.models.source.Genre, Genre>chunk(CHUNK_SIZE, transactionManager)
                .reader(genreReader)
                .processor(genreItemProcessor)
                .writer(genreWriter)
                .build();
    }

    //Book
    @Bean
    public RepositoryItemReader<ru.otus.hw.models.source.Book> bookReader(BookRepository bookRepository) {
        return new RepositoryItemReaderBuilder<ru.otus.hw.models.source.Book>()
                .name("bookReader")
                .repository(bookRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public MongoItemWriter<Book> bookWriter() {
        return new MongoItemWriterBuilder<Book>()
                .template(mongoTemplate)
                .build();
    }

    @Bean
    public BookItemProcessor bookItemProcessor() {
        return new BookItemProcessor(idCache);
    }

    @Bean
    public Step bookMigrationStep(RepositoryItemReader<ru.otus.hw.models.source.Book> bookReader,
                                   BookItemProcessor bookItemProcessor,
                                   MongoItemWriter<Book> bookWriter) {
        return new StepBuilder("bookMigrationStep", jobRepository)
                .<ru.otus.hw.models.source.Book, Book>chunk(CHUNK_SIZE, transactionManager)
                .reader(bookReader)
                .processor(bookItemProcessor)
                .writer(bookWriter)
                .build();
    }

    //BookComment
    @Bean
    public RepositoryItemReader<ru.otus.hw.models.source.BookComment> bookCommentReader(
            BookCommentRepository bookCommentRepository) {
        return new RepositoryItemReaderBuilder<ru.otus.hw.models.source.BookComment>()
                .name("bookCommentReader")
                .repository(bookCommentRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public MongoItemWriter<BookComment> bookCommentWriter() {
        return new MongoItemWriterBuilder<BookComment>()
                .template(mongoTemplate)
                .build();
    }

    @Bean
    public BookCommentItemProcessor bookCommentItemProcessor() {
        return new BookCommentItemProcessor(idCache);
    }

    @Bean
    public Step bookCommentMigrationStep(RepositoryItemReader<ru.otus.hw.models.source.BookComment> bookCommentReader,
                                          BookCommentItemProcessor bookCommentItemProcessor,
                                          MongoItemWriter<BookComment> bookCommentWriter) {
        return new StepBuilder("bookCommentMigrationStep", jobRepository)
                .<ru.otus.hw.models.source.BookComment, BookComment>chunk(CHUNK_SIZE, transactionManager)
                .reader(bookCommentReader)
                .processor(bookCommentItemProcessor)
                .writer(bookCommentWriter)
                .build();
    }

    //Job
    @Bean
    public CacheResetJobListener cacheResetJobListener(JobExplorer jobExplorer) {
        return new CacheResetJobListener(idCache, jobExplorer);
    }

    @Bean
    public Job migrationJob(Step authorMigrationStep,
                             Step genreMigrationStep,
                             Step bookMigrationStep,
                             Step bookCommentMigrationStep,
                             CacheResetJobListener cacheResetJobListener) {
        return new JobBuilder(MIGRATION_JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(cacheResetJobListener)
                .start(authorMigrationStep)
                .next(genreMigrationStep)
                .next(bookMigrationStep)
                .next(bookCommentMigrationStep)
                .build();
    }

}
