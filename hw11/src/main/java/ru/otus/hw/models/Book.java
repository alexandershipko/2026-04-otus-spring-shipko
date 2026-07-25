package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("books")
@ToString(exclude = {"author", "genres"})
@EqualsAndHashCode(exclude = {"author", "genres"})
public class Book {

    @Id
    private long id;

    @Column("title")
    private String title;

    @Column("author_id")
    private long authorId;

    @Transient
    private Author author;

    @Transient
    private List<Genre> genres;

}
