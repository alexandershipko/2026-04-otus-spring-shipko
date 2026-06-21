package ru.otus.hw.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import ru.otus.hw.logging.TrackTime;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;

@Repository
public class JpaBookRepository implements BookRepository {

    @PersistenceContext
    private final EntityManager em;

    public JpaBookRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Optional<Book> findById(long id) {
        EntityGraph<?> graph = em.getEntityGraph("book-entity-graph");
        return Optional.ofNullable(em.find(Book.class, id, Map.of(FETCH.getKey(), graph)));
    }

    @Override
    @TrackTime
    public List<Book> findAll() {
        EntityGraph<?> graph = em.getEntityGraph("book-entity-graph");
        TypedQuery<Book> query = em.createQuery("select distinct b from Book b", Book.class);
        query.setHint(FETCH.getKey(), graph);

        return query.getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            em.persist(book);

            return book;
        }

        return em.merge(book);
    }

    @Override
    public void deleteById(long id) {
        Book book = em.find(Book.class, id);

        if (book != null) {
            em.remove(book);
        }
    }

}
