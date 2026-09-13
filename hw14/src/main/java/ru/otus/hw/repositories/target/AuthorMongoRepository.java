package ru.otus.hw.repositories.target;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.target.Author;

public interface AuthorMongoRepository extends MongoRepository<Author, String> {

}
