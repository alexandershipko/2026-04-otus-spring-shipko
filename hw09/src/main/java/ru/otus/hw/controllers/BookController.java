package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final BookCommentService bookCommentService;

    @GetMapping
    public String findAll(Model model) {
        var books = bookService.findAll().stream()
                .map(BookController::toBookDto)
                .toList();
        model.addAttribute("books", books);

        return "books/list";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable long id, Model model) {
        var book = bookService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));
        var comments = bookCommentService.findAllByBookId(id).stream()
                .map(comment -> new BookCommentDto(comment.getId(), comment.getText()))
                .toList();
        model.addAttribute("book", toBookDto(book));
        model.addAttribute("comments", comments);

        return "books/view";
    }

    @GetMapping("/new")
    public String newBookForm(Model model) {
        model.addAttribute("bookForm", new BookFormDto());
        addFormReferenceData(model);

        return "books/form";
    }

    @PostMapping
    public String create(@ModelAttribute("bookForm") BookFormDto bookForm) {
        bookService.insert(bookForm.getTitle(), bookForm.getAuthorId(), bookForm.getGenreIds());

        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String editBookForm(@PathVariable long id, Model model) {
        var book = bookService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));
        var genreIds = book.getGenres().stream()
                .map(Genre::getId).collect(Collectors.toSet());
        var bookForm = new BookFormDto(book.getId(), book.getTitle(), book.getAuthor().getId(), genreIds);
        model.addAttribute("bookForm", bookForm);
        addFormReferenceData(model);

        return "books/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable long id, @ModelAttribute("bookForm") BookFormDto bookForm) {
        bookService.update(id, bookForm.getTitle(), bookForm.getAuthorId(), bookForm.getGenreIds());

        return "redirect:/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long id) {
        bookService.deleteById(id);

        return "redirect:/books";
    }

    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable long id, @RequestParam String text) {
        bookCommentService.insert(text, id);

        return "redirect:/books/%d".formatted(id);
    }

    @PostMapping("/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable long id, @PathVariable long commentId) {
        bookCommentService.deleteById(commentId);

        return "redirect:/books/%d".formatted(id);
    }

    private void addFormReferenceData(Model model) {
        var authors = authorService.findAll().stream()
                .map(BookController::toAuthorDto)
                .toList();
        var genres = genreService.findAll().stream()
                .map(BookController::toGenreDto)
                .toList();
        model.addAttribute("authors", authors);
        model.addAttribute("genres", genres);
    }

    private static BookDto toBookDto(Book book) {
        var genres = book.getGenres().stream()
                .map(BookController::toGenreDto)
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
