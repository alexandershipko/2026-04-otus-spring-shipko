package ru.otus.hw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("REST-контроллер книг")
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookCommentService bookCommentService;

    private final BookDto bookDto =
            new BookDto(1, "BookTitle_1", new AuthorDto(1, "Author_1"), List.of(new GenreDto(1, "Genre_1")));

    @DisplayName("должен возвращать список книг")
    @Test
    void shouldReturnAllBooks() throws Exception {
        given(bookService.findAll()).willReturn(List.of(bookDto));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("BookTitle_1"));
    }

    @DisplayName("должен возвращать книгу по id")
    @Test
    void shouldReturnBookById() throws Exception {
        given(bookService.findById(1L)).willReturn(bookDto);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("BookTitle_1"));
    }

    @DisplayName("должен возвращать 404 с JSON при отсутствии книги")
    @Test
    void shouldReturnNotFoundJsonForMissingBook() throws Exception {
        given(bookService.findById(99L)).willThrow(new EntityNotFoundException("Book with id 99 not found"));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book with id 99 not found"));
    }

    @DisplayName("должен создавать книгу")
    @Test
    void shouldCreateBook() throws Exception {
        var createDto = new BookCreateDto("NewBook", 1L, Set.of(1L));
        given(bookService.insert(createDto)).willReturn(bookDto);

        mockMvc.perform(post("/api/books")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/books/1"))
                .andExpect(jsonPath("$.title").value("BookTitle_1"));

        verify(bookService).insert(eq(createDto));
    }

    @DisplayName("должен возвращать 400 при невалидном теле создания книги")
    @Test
    void shouldReturnBadRequestForInvalidCreateBody() throws Exception {
        var invalidDto = new BookCreateDto("", null, Set.of());

        mockMvc.perform(post("/api/books")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.authorId").exists())
                .andExpect(jsonPath("$.errors.genreIds").exists());
    }

    @DisplayName("должен обновлять книгу, подставляя id из пути")
    @Test
    void shouldUpdateBook() throws Exception {
        var requestBody = new BookUpdateDto(0, "UpdatedBook", 1L, Set.of(1L));
        given(bookService.update(new BookUpdateDto(1L, "UpdatedBook", 1L, Set.of(1L)))).willReturn(bookDto);

        mockMvc.perform(put("/api/books/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("BookTitle_1"));

        verify(bookService).update(eq(new BookUpdateDto(1L, "UpdatedBook", 1L, Set.of(1L))));
    }

    @DisplayName("должен возвращать 400 при невалидном теле обновления книги")
    @Test
    void shouldReturnBadRequestForInvalidUpdateBody() throws Exception {
        var invalidDto = new BookUpdateDto(0, " ", null, Set.of());

        mockMvc.perform(put("/api/books/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.authorId").exists())
                .andExpect(jsonPath("$.errors.genreIds").exists());
    }

    @DisplayName("должен возвращать 400 с JSON для некорректного JSON")
    @Test
    void shouldReturnBadRequestJsonForMalformedBody() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("должен удалять книгу")
    @Test
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById(1L);
    }

    @DisplayName("должен возвращать 404 при удалении отсутствующей книги")
    @Test
    void shouldReturnNotFoundWhenDeletingMissingBook() throws Exception {
        doThrow(new EntityNotFoundException("Book with id 99 not found"))
                .when(bookService).deleteById(99L);

        mockMvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Book with id 99 not found"));
    }

    @DisplayName("должен возвращать комментарии книги")
    @Test
    void shouldReturnComments() throws Exception {
        given(bookCommentService.findAllByBookId(1L)).willReturn(List.of(new BookCommentDto(1, "Comment_1")));

        mockMvc.perform(get("/api/books/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Comment_1"));
    }

    @DisplayName("должен добавлять комментарий, подставляя id книги из пути")
    @Test
    void shouldAddComment() throws Exception {
        given(bookCommentService.insert(new BookCommentCreateDto("NewComment", 1L)))
                .willReturn(new BookCommentDto(2, "NewComment"));

        mockMvc.perform(post("/api/books/1/comments")
                        .contentType(APPLICATION_JSON)
                        .content("{\"text\":\"NewComment\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/books/1/comments/2"))
                .andExpect(jsonPath("$.text").value("NewComment"));

        verify(bookCommentService).insert(eq(new BookCommentCreateDto("NewComment", 1L)));
    }

    @DisplayName("должен возвращать 400 для пустого комментария")
    @Test
    void shouldReturnBadRequestForBlankComment() throws Exception {
        mockMvc.perform(post("/api/books/1/comments")
                        .contentType(APPLICATION_JSON)
                        .content("{\"text\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.text").exists());
    }

    @DisplayName("должен удалять комментарий")
    @Test
    void shouldDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/books/1/comments/2"))
                .andExpect(status().isNoContent());

        verify(bookCommentService).deleteByIdAndBookId(2L, 1L);
    }

    @DisplayName("должен учитывать книгу при удалении комментария")
    @Test
    void shouldReturnNotFoundWhenCommentBelongsToAnotherBook() throws Exception {
        doThrow(new EntityNotFoundException("Comment with id 2 not found for book with id 3"))
                .when(bookCommentService).deleteByIdAndBookId(2L, 3L);

        mockMvc.perform(delete("/api/books/3/comments/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Comment with id 2 not found for book with id 3"));
    }

    @DisplayName("должен возвращать 405 с JSON для неподдерживаемого метода")
    @Test
    void shouldReturnMethodNotAllowedJson() throws Exception {
        mockMvc.perform(post("/api/books/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").exists());
    }

}
