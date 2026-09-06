package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final AclPermissionService aclPermissionService;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'ru.otus.hw.models.Book', 'READ')")
    public BookDto findById(long id) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));

        return toBookDto(book);
    }

    @Override
    @Transactional(readOnly = true)
    @PostFilter("hasRole('ADMIN') or hasPermission(filterObject.id, 'ru.otus.hw.models.Book', 'READ')")
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(BookServiceImpl::toBookDto)
                .toList();
    }

    @Override
    @Transactional
    public BookDto insert(BookCreateDto bookCreateDto) {
        var genresIds = bookCreateDto.getGenreIds();
        var genres = findGenresOrThrow(genresIds);
        var author = findAuthorOrThrow(bookCreateDto.getAuthorId());

        var book = new Book(0, bookCreateDto.getTitle(), author, genres);
        var savedBook = bookRepository.save(book);

        aclPermissionService.grantOwnerPermissions(savedBook,
                BasePermission.READ, BasePermission.WRITE, BasePermission.DELETE);

        return toBookDto(savedBook);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#bookUpdateDto.id, 'ru.otus.hw.models.Book', 'WRITE')")
    public BookDto update(BookUpdateDto bookUpdateDto) {
        var genresIds = bookUpdateDto.getGenreIds();
        var genres = findGenresOrThrow(genresIds);
        var author = findAuthorOrThrow(bookUpdateDto.getAuthorId());

        var book = bookRepository.findById(bookUpdateDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookUpdateDto.getId())));

        book.setTitle(bookUpdateDto.getTitle());
        book.setAuthor(author);
        book.setGenres(genres);

        return toBookDto(bookRepository.save(book));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'ru.otus.hw.models.Book', 'DELETE')")
    public void deleteById(long id) {
        bookRepository.deleteById(id);

        aclPermissionService.deleteAcl(Book.class, id);
    }

    private Author findAuthorOrThrow(long authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
    }

    private List<Genre> findGenresOrThrow(Set<Long> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var genres = genreRepository.findAllByIds(genresIds);

        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        return genres;
    }

    private static BookDto toBookDto(Book book) {
        var genres = book.getGenres().stream()
                .map(BookServiceImpl::toGenreDto)
                .toList();

        return new BookDto(book.getId(), book.getTitle(), toAuthorDto(book.getAuthor()), genres);
    }

    private static AuthorDto toAuthorDto(Author author) {
        return new AuthorDto(author.getId(), author.getFullName());
    }

    private static GenreDto toGenreDto(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }

}
