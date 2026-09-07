package ru.otus.hw.controllers.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.services.BookService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Авторизация контроллера книг на основе владения и ACL")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookService bookService;

    @DisplayName("любой аутентифицированный пользователь может добавить книгу")
    @Test
    void anyUserCanCreateBook() throws Exception {
        mockMvc.perform(post("/books").with(csrf()).with(user("alice").roles("USER"))
                        .param("title", "Alice's book")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection());
    }

    @DisplayName("владелец может редактировать и удалять свою книгу")
    @Test
    void ownerCanEditAndDeleteOwnBook() throws Exception {
        mockMvc.perform(post("/books").with(csrf()).with(user("alice").roles("USER"))
                        .param("title", "Alice's own book")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection());

        long bookId = findBookIdByTitle("Alice's own book");

        mockMvc.perform(post("/books/" + bookId + "/edit").with(csrf()).with(user("alice").roles("USER"))
                        .param("title", "Updated by alice")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/books/" + bookId + "/delete").with(csrf()).with(user("alice").roles("USER")))
                .andExpect(status().is3xxRedirection());
    }

    @DisplayName("посторонний пользователь не должен иметь возможность редактировать или удалять чужую книгу")
    @Test
    void strangerCannotEditOrDeleteForeignBook() throws Exception {
        mockMvc.perform(post("/books").with(csrf()).with(user("alice").roles("USER"))
                        .param("title", "Alice's protected book")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection());

        long bookId = findBookIdByTitle("Alice's protected book");

        mockMvc.perform(post("/books/" + bookId + "/edit").with(csrf()).with(user("bob").roles("USER"))
                        .param("title", "Hacked")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/books/" + bookId + "/delete").with(csrf()).with(user("bob").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @DisplayName("обычный пользователь не должен иметь возможность редактировать или удалять книгу без владельца")
    @Test
    void regularUserCannotModifyOwnerlessBook() throws Exception {
        mockMvc.perform(post("/books/1/edit").with(csrf()).with(user("bob").roles("USER"))
                        .param("title", "Hacked")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/books/1/delete").with(csrf()).with(user("bob").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @DisplayName("администратор может редактировать и удалять книгу без владельца")
    @Test
    void adminCanModifyOwnerlessBook() throws Exception {
        mockMvc.perform(post("/books/1/edit").with(csrf()).with(user("admin").roles("ADMIN"))
                        .param("title", "Fixed by admin")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/books/1/delete").with(csrf()).with(user("admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection());
    }

    @DisplayName("владелец книги может добавить к ней комментарий")
    @Test
    void ownerCanAddCommentToOwnBook() throws Exception {
        mockMvc.perform(post("/books").with(csrf()).with(user("alice").roles("USER"))
                        .param("title", "Alice's commentable book")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection());

        long bookId = findBookIdByTitle("Alice's commentable book");

        mockMvc.perform(post("/books/" + bookId + "/comments").with(csrf()).with(user("alice").roles("USER"))
                        .param("text", "New comment"))
                .andExpect(status().is3xxRedirection());
    }

    @DisplayName("посторонний пользователь не должен иметь возможность комментировать чужую книгу")
    @Test
    void strangerCannotAddCommentToForeignBook() throws Exception {
        mockMvc.perform(post("/books").with(csrf()).with(user("alice").roles("USER"))
                        .param("title", "Alice's private book")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection());

        long bookId = findBookIdByTitle("Alice's private book");

        mockMvc.perform(post("/books/" + bookId + "/comments").with(csrf()).with(user("bob").roles("USER"))
                        .param("text", "Sneaky comment"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("обычный пользователь не должен иметь возможность комментировать книгу без владельца")
    @Test
    void regularUserCannotCommentOnOwnerlessBook() throws Exception {
        mockMvc.perform(post("/books/1/comments").with(csrf()).with(user("bob").roles("USER"))
                        .param("text", "Sneaky comment"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("администратор может комментировать книгу без владельца")
    @Test
    void adminCanCommentOnOwnerlessBook() throws Exception {
        mockMvc.perform(post("/books/1/comments").with(csrf()).with(user("admin").roles("ADMIN"))
                        .param("text", "Admin's comment"))
                .andExpect(status().is3xxRedirection());
    }

    @DisplayName("посторонний пользователь не должен иметь возможность удалить чужой комментарий")
    @Test
    void strangerCannotDeleteForeignComment() throws Exception {
        mockMvc.perform(post("/books/1/comments/1/delete").with(csrf()).with(user("bob").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @DisplayName("администратор должен иметь возможность удалить любой комментарий")
    @Test
    void adminCanDeleteAnyComment() throws Exception {
        mockMvc.perform(post("/books/1/comments/1/delete").with(csrf()).with(user("admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection());
    }

    private long findBookIdByTitle(String title) {
        Authentication previous = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", null, "ROLE_ADMIN"));
        try {
            return bookService.findAll().stream()
                    .filter(book -> book.getTitle().equals(title))
                    .findFirst()
                    .orElseThrow()
                    .getId();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(previous);
        }
    }

}
