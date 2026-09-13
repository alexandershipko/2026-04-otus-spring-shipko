package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import ru.otus.hw.batch.cache.MigrationIdCache;
import ru.otus.hw.models.target.Book;
import ru.otus.hw.models.target.BookComment;

@RequiredArgsConstructor
public class BookCommentItemProcessor implements ItemProcessor<ru.otus.hw.models.source.BookComment, BookComment> {

    private final MigrationIdCache idCache;

    @Override
    public BookComment process(@NonNull ru.otus.hw.models.source.BookComment source) {
        var targetId = new ObjectId().toHexString();
        var targetBookId = idCache.getBookId(source.getBook().getId());
        var targetBookRef = new Book();
        targetBookRef.setId(targetBookId);

        return new BookComment(targetId, source.getText(), targetBookRef);
    }

}
