package ru.otus.hw.repositories.source;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.source.Genre;

public interface GenreRepository extends JpaRepository<Genre, Long> {

}
