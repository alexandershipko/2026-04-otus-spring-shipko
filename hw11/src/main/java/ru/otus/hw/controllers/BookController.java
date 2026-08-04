package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCommentCreateDto;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    private final BookCommentService bookCommentService;

    @GetMapping
    public Flux<BookDto> findAll() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<BookDto> findById(@PathVariable String id) {
        return bookService.findById(id);
    }

    @PostMapping
    public Mono<ResponseEntity<BookDto>> create(@Valid @RequestBody BookCreateDto bookCreateDto) {
        return bookService.insert(bookCreateDto)
                .map(createdBook -> {
                    var location = URI.create("/api/books/%s".formatted(createdBook.getId()));

                    return ResponseEntity.created(location).body(createdBook);
                });
    }

    @PutMapping("/{id}")
    public Mono<BookDto> update(@PathVariable String id,
                                @Valid @RequestBody BookUpdateDto bookUpdateDto) {
        bookUpdateDto.setId(id);

        return bookService.update(bookUpdateDto);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return bookService.deleteById(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}/comments")
    public Flux<BookCommentDto> findComments(@PathVariable String id) {
        return bookCommentService.findAllByBookId(id);
    }

    @PostMapping("/{id}/comments")
    public Mono<ResponseEntity<BookCommentDto>> addComment(@PathVariable String id,
                                                           @Valid @RequestBody BookCommentCreateDto commentCreateDto) {
        commentCreateDto.setBookId(id);

        return bookCommentService.insert(commentCreateDto)
                .map(createdComment -> {
                    var location = URI.create("/api/books/%s/comments/%s".formatted(id, createdComment.getId()));

                    return ResponseEntity.created(location).body(createdComment);
                });
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public Mono<ResponseEntity<Void>> deleteComment(@PathVariable String id,
                                                    @PathVariable String commentId) {
        return bookCommentService.deleteByIdAndBookId(commentId, id)
                .thenReturn(ResponseEntity.noContent().build());
    }

}
