package ru.otus.hw.models.source;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
@ToString(exclude = {"author", "genres"})
@EqualsAndHashCode(exclude = {"author", "genres"})
@NamedEntityGraphs({
        @NamedEntityGraph(name = "book-author-graph",
                attributeNodes = {@NamedAttributeNode("author")}),
        @NamedEntityGraph(name = "book-author-genres-graph",
                attributeNodes = {@NamedAttributeNode("author"), @NamedAttributeNode("genres")})
})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "books_genres",
            joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Genre> genres;

}
