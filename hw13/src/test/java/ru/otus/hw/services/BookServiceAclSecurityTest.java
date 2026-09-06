package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;

import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Авторизация на основе ACL в сервисе книг")
@SpringBootTest
@Transactional
class BookServiceAclSecurityTest {

    @Autowired
    private BookService bookService;

    @DisplayName("обычный пользователь не должен иметь возможность добавить книгу")
    @Test
    void regularUserCannotInsertBook() {
        runAs("bob", "ROLE_USER", () ->
                assertThatThrownBy(() -> bookService.insert(new BookCreateDto("New book", 1L, Set.of(1L))))
                        .isInstanceOf(AccessDeniedException.class));
    }

    @DisplayName("администратор может добавить книгу, а затем изменить и удалить её")
    @Test
    void adminCanInsertUpdateAndDeleteBook() {
        var book = runAs("admin", "ROLE_ADMIN",
                () -> bookService.insert(new BookCreateDto("Added by admin", 1L, Set.of(1L))));

        runAs("admin", "ROLE_ADMIN", () -> {
            var updated = bookService.update(new BookUpdateDto(book.getId(), "Updated by admin", 1L, Set.of(1L)));
            assertThat(updated.getTitle()).isEqualTo("Updated by admin");

            bookService.deleteById(book.getId());
            return null;
        });
    }

    @DisplayName("обычный пользователь не должен иметь возможность изменять или удалять существующую книгу")
    @Test
    void regularUserCannotModifyExistingBook() {
        runAs("bob", "ROLE_USER", () -> {
            assertThatThrownBy(() -> bookService.update(new BookUpdateDto(1L, "Hacked", 1L, Set.of(1L))))
                    .isInstanceOf(AccessDeniedException.class);
            assertThatThrownBy(() -> bookService.deleteById(1L))
                    .isInstanceOf(AccessDeniedException.class);
            return null;
        });
    }

    @DisplayName("администратор может изменять и удалять любую существующую книгу")
    @Test
    void adminCanModifyExistingBook() {
        runAs("admin", "ROLE_ADMIN", () -> {
            var updated = bookService.update(new BookUpdateDto(1L, "Fixed by admin", 1L, Set.of(1L)));
            assertThat(updated.getTitle()).isEqualTo("Fixed by admin");

            bookService.deleteById(1L);
            return null;
        });
    }

    private <T> T runAs(String username, String authority, Supplier<T> action) {
        Authentication previous = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(username, null, authority));
        try {
            return action.get();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(previous);
        }
    }

}
