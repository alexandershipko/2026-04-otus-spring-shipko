package ru.otus.hw.repositories.source;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.source.Genre;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    List<Genre> findByIdGreaterThanOrderByIdAsc(long id, Pageable pageable);

}
