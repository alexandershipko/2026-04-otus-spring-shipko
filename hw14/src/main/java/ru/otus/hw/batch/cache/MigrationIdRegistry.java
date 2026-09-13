package ru.otus.hw.batch.cache;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MigrationIdRegistry {

    private static final String COLLECTION = "migration_ids";

    private final MongoTemplate mongoTemplate;

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public String getOrCreate(EntityType entityType, long sourceId) {
        return cache.computeIfAbsent(key(entityType, sourceId), key -> createOrFind(key, entityType, sourceId));
    }

    public String require(EntityType entityType, long sourceId) {
        return cache.computeIfAbsent(key(entityType, sourceId), key -> find(key, entityType, sourceId));
    }

    public void reset() {
        cache.clear();
    }

    private String createOrFind(String key, EntityType entityType, long sourceId) {
        var query = queryByKey(key);
        var update = new Update().setOnInsert("targetId", new ObjectId().toHexString());
        Document mapping;

        try {
            mapping = mongoTemplate.findAndModify(query, update,
                    FindAndModifyOptions.options().upsert(true).returnNew(true), Document.class, COLLECTION);
        } catch (DuplicateKeyException ex) {
            mapping = mongoTemplate.findOne(query, Document.class, COLLECTION);
        }

        return targetId(mapping, entityType, sourceId);
    }

    private String find(String key, EntityType entityType, long sourceId) {
        return targetId(mongoTemplate.findOne(queryByKey(key), Document.class, COLLECTION), entityType, sourceId);
    }

    private String key(EntityType entityType, long sourceId) {
        return "sourcedb:" + entityType.code() + ":" + sourceId;
    }

    private Query queryByKey(String key) {
        return Query.query(Criteria.where("_id").is(key));
    }

    private String targetId(Document mapping, EntityType entityType, long sourceId) {
        if (mapping == null || mapping.getString("targetId") == null) {
            throw new IllegalStateException("Не найден ID миграции для " + entityType.code() + ":" + sourceId);
        }

        return mapping.getString("targetId");
    }

}
