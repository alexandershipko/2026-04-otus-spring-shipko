package ru.otus.hw.repositories.source;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.source.Book;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("select b.id from Book b where b.id > :lastId order by b.id")
    List<Long> findMigrationIds(@Param("lastId") long lastId, Pageable pageable);

    @EntityGraph("book-author-genres-graph")
    @Query("select b from Book b where b.id in :ids")
    List<Book> findMigrationBooks(@Param("ids") List<Long> ids);

    @Transactional(readOnly = true)
    default List<Book> findMigrationBooksAfter(long lastId, Pageable pageable) {
        var ids = findMigrationIds(lastId, pageable);
        if (ids.isEmpty()) {
            return List.of();
        }
        var booksById = findMigrationBooks(ids).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        return ids.stream().map(id -> {
            var book = booksById.get(id);
            if (book == null) {
                throw new IllegalStateException("Книга исчезла во время миграции: " + id);
            }
            return book;
        }).toList();
    }

}
