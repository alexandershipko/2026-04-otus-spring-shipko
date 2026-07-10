package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.models.BookComment;

@Component
public class BookCommentConverter {
    public String commentToString(BookComment comment) {
        return "Id: %s, text: %s".formatted(comment.getId(), comment.getText());
    }
}
