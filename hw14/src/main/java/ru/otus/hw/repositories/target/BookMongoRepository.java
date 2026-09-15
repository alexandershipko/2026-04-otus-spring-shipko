package ru.otus.hw.repositories.target;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.target.Book;

public interface BookMongoRepository extends MongoRepository<Book, String> {

}
