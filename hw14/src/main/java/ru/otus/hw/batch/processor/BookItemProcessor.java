package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import ru.otus.hw.batch.cache.MigrationIdCache;
import ru.otus.hw.models.target.Author;
import ru.otus.hw.models.target.Book;
import ru.otus.hw.models.target.Genre;

@RequiredArgsConstructor
public class BookItemProcessor implements ItemProcessor<ru.otus.hw.models.source.Book, Book> {

    private final MigrationIdCache idCache;

    @Override
    public Book process(@NonNull ru.otus.hw.models.source.Book source) {
        var targetId = new ObjectId().toHexString();
        idCache.putBookId(source.getId(), targetId);

        var sourceAuthor = source.getAuthor();
        var targetAuthor = new Author(idCache.getAuthorId(sourceAuthor.getId()), sourceAuthor.getFullName());

        var targetGenres = source.getGenres().stream()
                .map(genre -> new Genre(idCache.getGenreId(genre.getId()), genre.getName()))
                .toList();

        return new Book(targetId, source.getTitle(), targetAuthor, targetGenres);
    }

}
