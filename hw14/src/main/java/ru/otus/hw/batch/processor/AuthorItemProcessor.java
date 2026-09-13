package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import ru.otus.hw.batch.cache.EntityType;
import ru.otus.hw.batch.cache.MigrationIdRegistry;
import ru.otus.hw.models.target.Author;

@RequiredArgsConstructor
public class AuthorItemProcessor implements ItemProcessor<ru.otus.hw.models.source.Author, Author> {

    private final MigrationIdRegistry idRegistry;

    @Override
    public Author process(@NonNull ru.otus.hw.models.source.Author source) {
        var targetId = idRegistry.getOrCreate(EntityType.AUTHOR, source.getId());

        return new Author(targetId, source.getFullName());
    }

}
