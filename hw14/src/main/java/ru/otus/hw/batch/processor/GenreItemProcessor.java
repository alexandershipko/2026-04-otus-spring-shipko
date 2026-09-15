package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import ru.otus.hw.batch.cache.EntityType;
import ru.otus.hw.batch.cache.MigrationIdRegistry;
import ru.otus.hw.models.target.Genre;

@RequiredArgsConstructor
public class GenreItemProcessor implements ItemProcessor<ru.otus.hw.models.source.Genre, Genre> {

    private final MigrationIdRegistry idRegistry;

    @Override
    public Genre process(@NonNull ru.otus.hw.models.source.Genre source) {
        var targetId = idRegistry.getOrCreate(EntityType.GENRE, source.getId());

        return new Genre(targetId, source.getName());
    }

}
