package ru.otus.hw.repositories.target;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.target.Genre;

public interface GenreMongoRepository extends MongoRepository<Genre, String> {

}
