package ru.otus.hw.repositories.target;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.target.BookComment;

public interface BookCommentMongoRepository extends MongoRepository<BookComment, String> {

}
