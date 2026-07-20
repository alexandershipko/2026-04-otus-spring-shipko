package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.BookCommentCreateDto;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    private final BookCommentService bookCommentService;

    @GetMapping
    public List<BookDto> findAll() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public BookDto findById(@PathVariable long id) {
        return bookService.findById(id);
    }

    @PostMapping
    public ResponseEntity<BookDto> create(@Valid @RequestBody BookCreateDto bookCreateDto) {
        var createdBook = bookService.insert(bookCreateDto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdBook.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdBook);
    }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable long id,
                          @Valid @RequestBody BookUpdateDto bookUpdateDto) {
        bookUpdateDto.setId(id);

        return bookService.update(bookUpdateDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        bookService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    public List<BookCommentDto> findComments(@PathVariable long id) {
        return bookCommentService.findAllByBookId(id);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<BookCommentDto> addComment(@PathVariable long id,
                                                     @Valid @RequestBody BookCommentCreateDto commentCreateDto) {
        commentCreateDto.setBookId(id);

        var createdComment = bookCommentService.insert(commentCreateDto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{commentId}")
                .buildAndExpand(createdComment.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdComment);
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable long id,
                                              @PathVariable long commentId) {
        bookCommentService.deleteByIdAndBookId(commentId, id);

        return ResponseEntity.noContent().build();
    }

}
