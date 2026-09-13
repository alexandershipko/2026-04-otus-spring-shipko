package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import ru.otus.hw.batch.cache.MigrationIdCache;
import ru.otus.hw.models.target.Genre;

@RequiredArgsConstructor
public class GenreItemProcessor implements ItemProcessor<ru.otus.hw.models.source.Genre, Genre> {

    private final MigrationIdCache idCache;

    @Override
    public Genre process(@NonNull ru.otus.hw.models.source.Genre source) {
        var targetId = new ObjectId().toHexString();
        idCache.putGenreId(source.getId(), targetId);

        return new Genre(targetId, source.getName());
    }

}
