package ru.otus.hw.repositories.source;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.source.Author;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    List<Author> findByIdGreaterThanOrderByIdAsc(long id, Pageable pageable);

}
