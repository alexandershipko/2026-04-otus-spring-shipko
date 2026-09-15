package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import ru.otus.hw.batch.cache.EntityType;
import ru.otus.hw.batch.cache.MigrationIdRegistry;
import ru.otus.hw.models.target.Author;
import ru.otus.hw.models.target.Book;
import ru.otus.hw.models.target.Genre;

@RequiredArgsConstructor
public class BookItemProcessor implements ItemProcessor<ru.otus.hw.models.source.Book, Book> {

    private final MigrationIdRegistry idRegistry;

    @Override
    public Book process(@NonNull ru.otus.hw.models.source.Book source) {
        var targetId = idRegistry.getOrCreate(EntityType.BOOK, source.getId());

        var sourceAuthor = source.getAuthor();
        var targetAuthor = new Author(idRegistry.require(EntityType.AUTHOR, sourceAuthor.getId()),
                sourceAuthor.getFullName());

        var targetGenres = source.getGenres().stream()
                .map(genre -> new Genre(idRegistry.require(EntityType.GENRE, genre.getId()), genre.getName()))
                .toList();

        return new Book(targetId, source.getTitle(), targetAuthor, targetGenres);
    }

}
