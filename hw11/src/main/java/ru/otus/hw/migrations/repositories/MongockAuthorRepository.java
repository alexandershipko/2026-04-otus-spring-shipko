package ru.otus.hw.migrations.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Author;

public interface MongockAuthorRepository extends MongoRepository<Author, String> {

}
