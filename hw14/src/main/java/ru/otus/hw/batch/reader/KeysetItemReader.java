package ru.otus.hw.batch.reader;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToLongFunction;

public class KeysetItemReader<T> extends AbstractItemStreamItemReader<T> {

    private static final String LAST_ID_KEY = "lastId";

    private final int pageSize;

    private final BiFunction<Long, Integer, List<T>> pageFetcher;

    private final ToLongFunction<T> idExtractor;

    private long lastId;

    private Iterator<T> currentPage = Collections.emptyIterator();

    private boolean exhausted;

    public KeysetItemReader(String name, int pageSize, BiFunction<Long, Integer, List<T>> pageFetcher,
                             ToLongFunction<T> idExtractor) {
        this.pageSize = pageSize;
        this.pageFetcher = pageFetcher;
        this.idExtractor = idExtractor;
        setName(name);
    }

    @Override
    public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        lastId = executionContext.getLong(getExecutionContextKey(LAST_ID_KEY), 0L);
    }

    @Override
    public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putLong(getExecutionContextKey(LAST_ID_KEY), lastId);
    }

    @Nullable
    @Override
    public T read() {
        if (!currentPage.hasNext()) {
            if (exhausted) {
                return null;
            }

            var page = pageFetcher.apply(lastId, pageSize);
            if (page.isEmpty()) {
                exhausted = true;
                return null;
            }

            currentPage = page.iterator();
        }

        var item = currentPage.next();
        lastId = idExtractor.applyAsLong(item);

        return item;
    }

}
