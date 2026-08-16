package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.hw.dto.BookCommentCreateDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final BookCommentService bookCommentService;

    @GetMapping("/books")
    public String findAll(Model model) {
        model.addAttribute("books", bookService.findAll());

        return "books/list";
    }

    @GetMapping("/books/{id}")
    public String findById(@PathVariable long id, Model model) {
        model.addAttribute("book", bookService.findById(id));
        model.addAttribute("comments", bookCommentService.findAllByBookId(id));

        return "books/view";
    }

    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("bookForm", new BookCreateDto());
        model.addAttribute("formAction", "/books");
        addFormReferenceData(model);

        return "books/form";
    }

    @PostMapping("/books")
    public String create(@Valid @ModelAttribute("bookForm") BookCreateDto bookForm, BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/books");
            addFormReferenceData(model);

            return "books/form";
        }

        bookService.insert(bookForm);

        return "redirect:/books";
    }

    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable long id, Model model) {
        var book = bookService.findById(id);
        var genreIds = book.getGenres().stream()
                .map(GenreDto::getId).collect(Collectors.toSet());
        var bookForm = new BookUpdateDto(book.getId(), book.getTitle(), book.getAuthor().getId(), genreIds);

        model.addAttribute("bookForm", bookForm);
        model.addAttribute("formAction", "/books/%d/edit".formatted(id));

        addFormReferenceData(model);

        return "books/form";
    }

    @PostMapping("/books/{id}/edit")
    public String update(@PathVariable long id, @Valid @ModelAttribute("bookForm") BookUpdateDto bookForm,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/books/%d/edit".formatted(id));
            addFormReferenceData(model);

            return "books/form";
        }

        bookService.update(bookForm);

        return "redirect:/books";
    }

    @PostMapping("/books/{id}/delete")
    public String delete(@PathVariable long id) {
        bookService.deleteById(id);

        return "redirect:/books";
    }

    @PostMapping("/books/{id}/comments")
    public String addComment(@PathVariable long id, @RequestParam String text) {
        bookCommentService.insert(new BookCommentCreateDto(text, id));

        return "redirect:/books/%d".formatted(id);
    }

    @PostMapping("/books/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable long id, @PathVariable long commentId) {
        bookCommentService.deleteById(commentId);

        return "redirect:/books/%d".formatted(id);
    }

    private void addFormReferenceData(Model model) {
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
    }

}
