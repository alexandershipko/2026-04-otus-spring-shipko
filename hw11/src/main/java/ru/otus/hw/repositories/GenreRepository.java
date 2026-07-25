package ru.otus.hw.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Genre;

import java.util.Set;

public interface GenreRepository extends R2dbcRepository<Genre, Long> {

    @Query("select * from genres where id in (:ids)")
    Flux<Genre> findAllByIds(Set<Long> ids);

}
