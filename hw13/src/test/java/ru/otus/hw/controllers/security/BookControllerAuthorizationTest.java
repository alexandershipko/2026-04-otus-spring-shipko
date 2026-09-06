package ru.otus.hw.controllers.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Авторизация контроллера книг на основе ролей и ACL")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("обычный пользователь не должен иметь возможность добавить книгу")
    @Test
    @WithMockUser(roles = "USER")
    void regularUserCannotCreateBook() throws Exception {
        mockMvc.perform(post("/books").with(csrf())
                        .param("title", "New book")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("администратор должен иметь возможность добавить книгу")
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateBook() throws Exception {
        mockMvc.perform(post("/books").with(csrf())
                        .param("title", "New book")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection());
    }

    @DisplayName("обычный пользователь не должен иметь возможность редактировать существующую книгу")
    @Test
    @WithMockUser(roles = "USER")
    void regularUserCannotEditExistingBook() throws Exception {
        mockMvc.perform(post("/books/1/edit").with(csrf())
                        .param("title", "Hacked")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("администратор должен иметь возможность редактировать существующую книгу")
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanEditExistingBook() throws Exception {
        mockMvc.perform(post("/books/1/edit").with(csrf())
                        .param("title", "Fixed by admin")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection());
    }

    @DisplayName("обычный пользователь не должен иметь возможность удалить существующую книгу")
    @Test
    @WithMockUser(roles = "USER")
    void regularUserCannotDeleteExistingBook() throws Exception {
        mockMvc.perform(post("/books/1/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @DisplayName("администратор должен иметь возможность удалить существующую книгу")
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteExistingBook() throws Exception {
        mockMvc.perform(post("/books/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @DisplayName("любой аутентифицированный пользователь может добавить комментарий")
    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void anyUserCanAddComment() throws Exception {
        mockMvc.perform(post("/books/1/comments").with(csrf())
                        .param("text", "New comment"))
                .andExpect(status().is3xxRedirection());
    }

    @DisplayName("посторонний пользователь не должен иметь возможность удалить чужой комментарий")
    @Test
    @WithMockUser(username = "bob", roles = "USER")
    void strangerCannotDeleteForeignComment() throws Exception {
        mockMvc.perform(post("/books/1/comments/1/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @DisplayName("администратор должен иметь возможность удалить любой комментарий")
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteAnyComment() throws Exception {
        mockMvc.perform(post("/books/1/comments/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

}
