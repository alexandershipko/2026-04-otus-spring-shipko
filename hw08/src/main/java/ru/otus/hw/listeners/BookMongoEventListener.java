package ru.otus.hw.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookCommentRepository;

@Component
@RequiredArgsConstructor
public class BookMongoEventListener extends AbstractMongoEventListener<Book> {

    private final BookCommentRepository bookCommentRepository;

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<Book> event) {
        var idValue = event.getSource().get("_id");

        if (idValue != null) {
            bookCommentRepository.deleteAllByBookId(idValue.toString());
        }
    }

}
