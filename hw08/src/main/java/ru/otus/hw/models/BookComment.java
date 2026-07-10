package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "book_comments")
@EqualsAndHashCode(of = "id")
public class BookComment {

    @Id
    private String id;

    private String text;

    @DBRef(lazy = true)
    private Book book;

}
