package ru.otus.hw.migrations.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Book;

public interface MongockBookRepository extends MongoRepository<Book, String> {

}
