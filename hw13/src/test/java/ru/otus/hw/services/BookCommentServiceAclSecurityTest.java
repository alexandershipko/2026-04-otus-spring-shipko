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
import ru.otus.hw.dto.BookCommentCreateDto;
import ru.otus.hw.dto.BookCreateDto;

import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Авторизация на основе ACL в сервисе комментариев")
@SpringBootTest
@Transactional
class BookCommentServiceAclSecurityTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookCommentService bookCommentService;

    @DisplayName("владелец книги может добавить к ней комментарий")
    @Test
    void ownerCanAddCommentToOwnBook() {
        var book = runAs("alice", "ROLE_USER",
                () -> bookService.insert(new BookCreateDto("Alice's book", 1L, Set.of(1L))));

        var comment = runAs("alice", "ROLE_USER",
                () -> bookCommentService.insert(new BookCommentCreateDto("Nice book", book.getId())));

        assertThat(comment.getText()).isEqualTo("Nice book");
    }

    @DisplayName("посторонний пользователь не должен иметь возможность добавить комментарий к чужой книге")
    @Test
    void strangerCannotAddCommentToForeignBook() {
        var book = runAs("alice", "ROLE_USER",
                () -> bookService.insert(new BookCreateDto("Alice's book", 1L, Set.of(1L))));

        runAs("bob", "ROLE_USER", () ->
                assertThatThrownBy(() ->
                        bookCommentService.insert(new BookCommentCreateDto("Sneaky comment", book.getId())))
                        .isInstanceOf(AccessDeniedException.class));
    }

    @DisplayName("администратор может добавить комментарий к любой книге")
    @Test
    void adminCanAddCommentToAnyBook() {
        var book = runAs("alice", "ROLE_USER",
                () -> bookService.insert(new BookCreateDto("Alice's book", 1L, Set.of(1L))));

        var comment = runAs("admin", "ROLE_ADMIN",
                () -> bookCommentService.insert(new BookCommentCreateDto("Admin's comment", book.getId())));

        assertThat(comment.getText()).isEqualTo("Admin's comment");
    }

    @DisplayName("обычный пользователь не должен иметь возможность комментировать книгу без владельца")
    @Test
    void regularUserCannotCommentOnOwnerlessBook() {
        runAs("bob", "ROLE_USER", () ->
                assertThatThrownBy(() ->
                        bookCommentService.insert(new BookCommentCreateDto("Sneaky comment", 1L)))
                        .isInstanceOf(AccessDeniedException.class));
    }

    @DisplayName("администратор может комментировать книгу без владельца")
    @Test
    void adminCanCommentOnOwnerlessBook() {
        var comment = runAs("admin", "ROLE_ADMIN",
                () -> bookCommentService.insert(new BookCommentCreateDto("Admin's comment", 1L)));

        assertThat(comment.getText()).isEqualTo("Admin's comment");
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
