package ru.otus.hw.controllers.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.controllers.BookController;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("Правила доступа контроллера книг")
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerSecurityTest {

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

    @DisplayName("должен перенаправлять неаутентифицированного пользователя на страницу логина при просмотре списка книг")
    @Test
    void shouldRedirectAnonymousUserToLoginForBooksList() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @DisplayName("должен перенаправлять неаутентифицированного пользователя на страницу логина при просмотре книги")
    @Test
    void shouldRedirectAnonymousUserToLoginForBookView() throws Exception {
        mockMvc.perform(get("/books/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @DisplayName("не должен позволять неаутентифицированному пользователю удалять книгу")
    @Test
    void shouldRedirectAnonymousUserForDeleteBook() throws Exception {
        mockMvc.perform(post("/books/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verify(bookService, never()).deleteById(anyLong());
    }

    @DisplayName("должен отклонять POST-запрос без CSRF-токена")
    @Test
    void shouldRejectPostWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/books/1/delete"))
                .andExpect(status().isForbidden());

        verify(bookService, never()).deleteById(anyLong());
    }

    @DisplayName("страница логина должна быть доступна неаутентифицированному пользователю")
    @Test
    void shouldAllowAnonymousAccessToLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

}
