package ru.otus.hw.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import ru.otus.hw.migrations.repositories.MongockBookCommentRepository;
import ru.otus.hw.migrations.repositories.MongockBookRepository;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ChangeUnit(id = "insert-book-comments", order = "004", author = "shipko")
public class InsertBookCommentsChangeUnit {

    private static final Set<String> TEXTS = Set.of("Comment_1", "Comment_2", "Comment_3");

    @Execution
    public void execution(MongockBookRepository bookRepository, MongockBookCommentRepository bookCommentRepository) {
        var booksByTitle = bookRepository.findAll().stream()
                .collect(Collectors.toMap(Book::getTitle, Function.identity()));

        var book1 = booksByTitle.get("BookTitle_1");
        var book2 = booksByTitle.get("BookTitle_2");

        bookCommentRepository.saveAll(List.of(
                new BookComment(null, "Comment_1", book1.getId()),
                new BookComment(null, "Comment_2", book1.getId()),
                new BookComment(null, "Comment_3", book2.getId())
        ));
    }

    @RollbackExecution
    public void rollbackExecution(MongockBookCommentRepository bookCommentRepository) {
        var commentsToDelete = bookCommentRepository.findAll().stream()
                .filter(comment -> TEXTS.contains(comment.getText()))
                .toList();

        bookCommentRepository.deleteAll(commentsToDelete);
    }

}
