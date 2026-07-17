package ru.otus.hw.controllers;

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
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("Контроллер книг")
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private BookCommentService bookCommentService;

    private final AuthorDto authorDto = new AuthorDto(1, "Author_1");

    private final GenreDto genreDto = new GenreDto(1, "Genre_1");

    private final BookDto bookDto =
            new BookDto(1, "BookTitle_1", new AuthorDto(1, "Author_1"), List.of(new GenreDto(1, "Genre_1")));

    @DisplayName("должен отображать список книг")
    @Test
    void shouldReturnBooksList() throws Exception {
        given(bookService.findAll()).willReturn(List.of(bookDto));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attribute("books", List.of(bookDto)));
    }

    @DisplayName("должен отображать книгу с комментариями")
    @Test
    void shouldReturnBookView() throws Exception {
        given(bookService.findById(1L)).willReturn(bookDto);
        given(bookCommentService.findAllByBookId(1L)).willReturn(List.of(new BookCommentDto(1, "Comment_1")));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/view"))
                .andExpect(model().attribute("book", bookDto))
                .andExpect(model().attribute("comments", List.of(new BookCommentDto(1, "Comment_1"))));
    }

    @DisplayName("должен возвращать 404 при отсутствии книги")
    @Test
    void shouldReturnNotFoundForMissingBook() throws Exception {
        given(bookService.findById(99L)).willThrow(new EntityNotFoundException("Book with id 99 not found"));

        mockMvc.perform(get("/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @DisplayName("должен отображать форму создания книги")
    @Test
    void shouldReturnNewBookForm() throws Exception {
        given(authorService.findAll()).willReturn(List.of(authorDto));
        given(genreService.findAll()).willReturn(List.of(genreDto));

        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attribute("authors", List.of(new AuthorDto(1, "Author_1"))))
                .andExpect(model().attribute("genres", List.of(new GenreDto(1, "Genre_1"))));
    }

    @DisplayName("должен создавать книгу и делать редирект на список")
    @Test
    void shouldCreateBook() throws Exception {
        given(bookService.insert(any())).willReturn(bookDto);

        mockMvc.perform(post("/books")
                        .param("title", "NewBook")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).insert(eq(new BookCreateDto("NewBook", 1L, Set.of(1L))));
    }

    @DisplayName("должен отображать форму редактирования книги")
    @Test
    void shouldReturnEditBookForm() throws Exception {
        given(bookService.findById(1L)).willReturn(bookDto);
        given(authorService.findAll()).willReturn(List.of(authorDto));
        given(genreService.findAll()).willReturn(List.of(genreDto));

        mockMvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("bookForm"));
    }

    @DisplayName("должен возвращать 404 при редактировании отсутствующей книги")
    @Test
    void shouldReturnNotFoundForMissingBookEdit() throws Exception {
        given(bookService.findById(99L)).willThrow(new EntityNotFoundException("Book with id 99 not found"));

        mockMvc.perform(get("/books/99/edit"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @DisplayName("должен обновлять книгу и делать редирект на список")
    @Test
    void shouldUpdateBook() throws Exception {
        given(bookService.update(any())).willReturn(bookDto);

        mockMvc.perform(post("/books/1/edit")
                        .param("title", "UpdatedBook")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).update(eq(new BookUpdateDto(1L, "UpdatedBook", 1L, Set.of(1L))));
    }

    @DisplayName("должен удалять книгу и делать редирект на список")
    @Test
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(post("/books/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).deleteById(1L);
    }

    @DisplayName("не должен удалять книгу по GET-запросу")
    @Test
    void shouldNotAllowGetForDelete() throws Exception {
        mockMvc.perform(get("/books/1/delete"))
                .andExpect(status().isMethodNotAllowed());

        verify(bookService, never()).deleteById(anyLong());
    }

    @DisplayName("должен добавлять комментарий и делать редирект на страницу книги")
    @Test
    void shouldAddComment() throws Exception {
        mockMvc.perform(post("/books/1/comments")
                        .param("text", "NewComment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));

        verify(bookCommentService).insert(new BookCommentCreateDto("NewComment", 1L));
    }

    @DisplayName("должен удалять комментарий и делать редирект на страницу книги")
    @Test
    void shouldDeleteComment() throws Exception {
        mockMvc.perform(post("/books/1/comments/2/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));

        verify(bookCommentService).deleteById(2L);
    }

    @DisplayName("не должен удалять комментарий по GET-запросу")
    @Test
    void shouldNotAllowGetForCommentDelete() throws Exception {
        mockMvc.perform(get("/books/1/comments/2/delete"))
                .andExpect(status().isMethodNotAllowed());

        verify(bookCommentService, never()).deleteById(anyLong());
    }

    @DisplayName("должен показывать текст на русском по умолчанию")
    @Test
    void shouldRenderRussianTextByDefault() throws Exception {
        given(bookService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Список книг")));
    }

    @DisplayName("должен показывать текст на английском при переключении локали")
    @Test
    void shouldRenderEnglishTextWhenLocaleSwitched() throws Exception {
        given(bookService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/books").param("lang", "en_US"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Book list")));
    }

}
