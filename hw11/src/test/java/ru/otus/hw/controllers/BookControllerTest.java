package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCommentCreateDto;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@DisplayName("REST-контроллер книг")
@WebFluxTest(BookController.class)
class BookControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookCommentService bookCommentService;

    private final BookDto bookDto =
            new BookDto(1, "BookTitle_1", new AuthorDto(1, "Author_1"), List.of(new GenreDto(1, "Genre_1")));

    @DisplayName("должен возвращать список книг")
    @Test
    void shouldReturnAllBooks() {
        given(bookService.findAll()).willReturn(Flux.just(bookDto));

        webTestClient.get().uri("/api/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].title").isEqualTo("BookTitle_1");
    }

    @DisplayName("должен возвращать книгу по id")
    @Test
    void shouldReturnBookById() {
        given(bookService.findById(1L)).willReturn(Mono.just(bookDto));

        webTestClient.get().uri("/api/books/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("BookTitle_1");
    }

    @DisplayName("должен возвращать 404 с JSON при отсутствии книги")
    @Test
    void shouldReturnNotFoundJsonForMissingBook() {
        given(bookService.findById(99L))
                .willReturn(Mono.error(new EntityNotFoundException("Book with id 99 not found")));

        webTestClient.get().uri("/api/books/99")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Book with id 99 not found");
    }

    @DisplayName("должен создавать книгу")
    @Test
    void shouldCreateBook() {
        var createDto = new BookCreateDto("NewBook", 1L, Set.of(1L));
        given(bookService.insert(createDto)).willReturn(Mono.just(bookDto));

        webTestClient.post().uri("/api/books")
                .contentType(APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "/api/books/1")
                .expectBody()
                .jsonPath("$.title").isEqualTo("BookTitle_1");

        verify(bookService).insert(eq(createDto));
    }

    @DisplayName("должен возвращать 400 при невалидном теле создания книги")
    @Test
    void shouldReturnBadRequestForInvalidCreateBody() {
        var invalidDto = new BookCreateDto("", null, Set.of());

        webTestClient.post().uri("/api/books")
                .contentType(APPLICATION_JSON)
                .bodyValue(invalidDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.title").exists()
                .jsonPath("$.errors.authorId").exists()
                .jsonPath("$.errors.genreIds").exists();
    }

    @DisplayName("должен обновлять книгу, подставляя id из пути")
    @Test
    void shouldUpdateBook() {
        var requestBody = new BookUpdateDto(0, "UpdatedBook", 1L, Set.of(1L));
        given(bookService.update(new BookUpdateDto(1L, "UpdatedBook", 1L, Set.of(1L))))
                .willReturn(Mono.just(bookDto));

        webTestClient.put().uri("/api/books/1")
                .contentType(APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("BookTitle_1");

        verify(bookService).update(eq(new BookUpdateDto(1L, "UpdatedBook", 1L, Set.of(1L))));
    }

    @DisplayName("должен возвращать 400 при невалидном теле обновления книги")
    @Test
    void shouldReturnBadRequestForInvalidUpdateBody() {
        var invalidDto = new BookUpdateDto(0, " ", null, Set.of());

        webTestClient.put().uri("/api/books/1")
                .contentType(APPLICATION_JSON)
                .bodyValue(invalidDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.errors.title").exists()
                .jsonPath("$.errors.authorId").exists()
                .jsonPath("$.errors.genreIds").exists();
    }

    @DisplayName("должен возвращать 400 с JSON для некорректного JSON")
    @Test
    void shouldReturnBadRequestJsonForMalformedBody() {
        webTestClient.post().uri("/api/books")
                .contentType(APPLICATION_JSON)
                .bodyValue("{".getBytes(StandardCharsets.UTF_8))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").exists();
    }

    @DisplayName("должен удалять книгу")
    @Test
    void shouldDeleteBook() {
        given(bookService.deleteById(1L)).willReturn(Mono.empty());

        webTestClient.delete().uri("/api/books/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(bookService).deleteById(1L);
    }

    @DisplayName("должен возвращать 404 при удалении отсутствующей книги")
    @Test
    void shouldReturnNotFoundWhenDeletingMissingBook() {
        given(bookService.deleteById(99L))
                .willReturn(Mono.error(new EntityNotFoundException("Book with id 99 not found")));

        webTestClient.delete().uri("/api/books/99")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Book with id 99 not found");
    }

    @DisplayName("должен возвращать комментарии книги")
    @Test
    void shouldReturnComments() {
        given(bookCommentService.findAllByBookId(1L)).willReturn(Flux.just(new BookCommentDto(1, "Comment_1")));

        webTestClient.get().uri("/api/books/1/comments")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].text").isEqualTo("Comment_1");
    }

    @DisplayName("должен добавлять комментарий, подставляя id книги из пути")
    @Test
    void shouldAddComment() {
        given(bookCommentService.insert(new BookCommentCreateDto("NewComment", 1L)))
                .willReturn(Mono.just(new BookCommentDto(2, "NewComment")));

        webTestClient.post().uri("/api/books/1/comments")
                .contentType(APPLICATION_JSON)
                .bodyValue(new BookCommentCreateDto("NewComment", 0))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "/api/books/1/comments/2")
                .expectBody()
                .jsonPath("$.text").isEqualTo("NewComment");

        verify(bookCommentService).insert(eq(new BookCommentCreateDto("NewComment", 1L)));
    }

    @DisplayName("должен возвращать 400 для пустого комментария")
    @Test
    void shouldReturnBadRequestForBlankComment() {
        webTestClient.post().uri("/api/books/1/comments")
                .contentType(APPLICATION_JSON)
                .bodyValue(new BookCommentCreateDto(" ", 0))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.text").exists();
    }

    @DisplayName("должен удалять комментарий")
    @Test
    void shouldDeleteComment() {
        given(bookCommentService.deleteByIdAndBookId(2L, 1L)).willReturn(Mono.empty());

        webTestClient.delete().uri("/api/books/1/comments/2")
                .exchange()
                .expectStatus().isNoContent();

        verify(bookCommentService).deleteByIdAndBookId(2L, 1L);
    }

    @DisplayName("должен учитывать книгу при удалении комментария")
    @Test
    void shouldReturnNotFoundWhenCommentBelongsToAnotherBook() {
        given(bookCommentService.deleteByIdAndBookId(2L, 3L))
                .willReturn(Mono.error(new EntityNotFoundException("Comment with id 2 not found for book with id 3")));

        webTestClient.delete().uri("/api/books/3/comments/2")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Comment with id 2 not found for book with id 3");
    }

    @DisplayName("должен возвращать 405 с JSON для неподдерживаемого метода")
    @Test
    void shouldReturnMethodNotAllowedJson() {
        webTestClient.post().uri("/api/books/1")
                .exchange()
                .expectStatus().isEqualTo(405)
                .expectBody()
                .jsonPath("$.message").exists();
    }

}
