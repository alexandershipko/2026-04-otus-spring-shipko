package ru.otus.hw.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;

@Repository
@RequiredArgsConstructor
public class JpaBookCommentRepository implements BookCommentRepository {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public Optional<BookComment> findById(long id) {
        EntityGraph<?> graph = em.getEntityGraph("book-comment-book-graph");
        TypedQuery<BookComment> query = em.createQuery(
                "select bc from BookComment bc where bc.id = :id",
                BookComment.class);
        query.setParameter("id", id);
        query.setHint(FETCH.getKey(), graph);

        return query.getResultList().stream().findFirst();
    }

    @Override
    public List<BookComment> findAllByBookId(long bookId) {
        EntityGraph<?> graph = em.getEntityGraph("book-comment-book-graph");
        TypedQuery<BookComment> query = em.createQuery(
                "select bc from BookComment bc where bc.book.id = :bookId",
                BookComment.class);
        query.setParameter("bookId", bookId);
        query.setHint(FETCH.getKey(), graph);

        return query.getResultList();
    }

    @Override
    public BookComment save(BookComment comment) {
        if (comment.getId() == 0) {
            em.persist(comment);

            return comment;
        }

        return em.merge(comment);
    }

    @Override
    public void deleteById(long id) {
        BookComment comment = em.find(BookComment.class, id);

        if (comment != null) {
            em.remove(comment);
        }
    }

}
