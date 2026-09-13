package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import ru.otus.hw.batch.cache.EntityType;
import ru.otus.hw.batch.cache.MigrationIdRegistry;
import ru.otus.hw.models.target.Book;
import ru.otus.hw.models.target.BookComment;

@RequiredArgsConstructor
public class BookCommentItemProcessor implements ItemProcessor<ru.otus.hw.models.source.BookComment, BookComment> {

    private final MigrationIdRegistry idRegistry;

    @Override
    public BookComment process(@NonNull ru.otus.hw.models.source.BookComment source) {
        var targetId = idRegistry.getOrCreate(EntityType.COMMENT, source.getId());
        var targetBookId = idRegistry.require(EntityType.BOOK, source.getBook().getId());
        var targetBookRef = new Book();
        targetBookRef.setId(targetBookId);

        return new BookComment(targetId, source.getText(), targetBookRef);
    }

}
