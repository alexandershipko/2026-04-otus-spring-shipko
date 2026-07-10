package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.DocumentNotFoundException;
import ru.otus.hw.logging.TrackTime;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    public Optional<Book> findById(String id) {
        return bookRepository.findById(id);
    }

    @Override
    @TrackTime
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Book insert(String title, String authorId, Set<String> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new DocumentNotFoundException("Author with id %s not found".formatted(authorId)));
        var genres = genreRepository.findAllById(genresIds);

        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new DocumentNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        var book = new Book(null, title, author, genres);

        return bookRepository.save(book);
    }

    @Override
    public Book update(String id, String title, String authorId, Set<String> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var book = bookRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Book with id %s not found".formatted(id)));
        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new DocumentNotFoundException("Author with id %s not found".formatted(authorId)));
        var genres = genreRepository.findAllById(genresIds);

        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new DocumentNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        book.setTitle(title);
        book.setAuthor(author);
        book.setGenres(genres);

        return bookRepository.save(book);
    }

    @Override
    public void deleteById(String id) {
        bookRepository.deleteById(id);
    }

}
