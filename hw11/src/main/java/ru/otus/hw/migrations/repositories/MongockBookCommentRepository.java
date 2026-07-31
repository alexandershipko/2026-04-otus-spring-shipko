package ru.otus.hw.migrations.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.BookComment;

public interface MongockBookCommentRepository extends MongoRepository<BookComment, String> {

}
