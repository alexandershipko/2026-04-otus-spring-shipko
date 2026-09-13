package ru.otus.hw.repositories.source;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.source.BookComment;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {

}
