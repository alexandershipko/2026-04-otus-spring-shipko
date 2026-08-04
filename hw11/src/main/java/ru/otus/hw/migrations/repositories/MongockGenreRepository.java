package ru.otus.hw.migrations.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Genre;

public interface MongockGenreRepository extends MongoRepository<Genre, String> {

}
