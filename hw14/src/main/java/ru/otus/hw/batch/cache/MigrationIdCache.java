package ru.otus.hw.batch.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MigrationIdCache {

    private final Map<Long, String> authorIds = new ConcurrentHashMap<>();
    private final Map<Long, String> genreIds = new ConcurrentHashMap<>();
    private final Map<Long, String> bookIds = new ConcurrentHashMap<>();

    public void putAuthorId(long sourceId, String targetId) {
        authorIds.put(sourceId, targetId);
    }

    public String getAuthorId(long sourceId) {
        return authorIds.get(sourceId);
    }

    public void putGenreId(long sourceId, String targetId) {
        genreIds.put(sourceId, targetId);
    }

    public String getGenreId(long sourceId) {
        return genreIds.get(sourceId);
    }

    public void putBookId(long sourceId, String targetId) {
        bookIds.put(sourceId, targetId);
    }

    public String getBookId(long sourceId) {
        return bookIds.get(sourceId);
    }

    public void clear() {
        authorIds.clear();
        genreIds.clear();
        bookIds.clear();
    }

}
