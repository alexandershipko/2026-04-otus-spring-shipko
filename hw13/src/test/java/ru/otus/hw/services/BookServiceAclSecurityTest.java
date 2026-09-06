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
import ru.otus.hw.dto.BookDto;
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

    @DisplayName("владелец может читать, изменять и удалять свою книгу")
    @Test
    void ownerCanReadUpdateAndDeleteOwnBook() {
        var book = runAs("alice", "ROLE_USER",
                () -> bookService.insert(new BookCreateDto("Added by Alice", 1L, Set.of(1L))));

        runAs("alice", "ROLE_USER", () -> {
            var found = bookService.findById(book.getId());
            assertThat(found.getTitle()).isEqualTo("Added by Alice");

            var updated = bookService.update(new BookUpdateDto(book.getId(), "Updated by Alice", 1L, Set.of(1L)));
            assertThat(updated.getTitle()).isEqualTo("Updated by Alice");

            bookService.deleteById(book.getId());
            return null;
        });
    }

    @DisplayName("посторонний пользователь не должен иметь доступа к чужой книге ни на чтение, ни на запись")
    @Test
    void strangerCannotAccessForeignBook() {
        var book = runAs("alice", "ROLE_USER",
                () -> bookService.insert(new BookCreateDto("Added by Alice", 1L, Set.of(1L))));

        runAs("bob", "ROLE_USER", () -> {
            assertThatThrownBy(() -> bookService.findById(book.getId()))
                    .isInstanceOf(AccessDeniedException.class);
            assertThatThrownBy(() -> bookService.update(new BookUpdateDto(book.getId(), "Hacked", 1L, Set.of(1L))))
                    .isInstanceOf(AccessDeniedException.class);
            assertThatThrownBy(() -> bookService.deleteById(book.getId()))
                    .isInstanceOf(AccessDeniedException.class);
            return null;
        });
    }

    @DisplayName("администратор может читать, изменять и удалять чужую книгу")
    @Test
    void adminCanAccessForeignBook() {
        var book = runAs("alice", "ROLE_USER",
                () -> bookService.insert(new BookCreateDto("Added by Alice", 1L, Set.of(1L))));

        runAs("admin", "ROLE_ADMIN", () -> {
            var found = bookService.findById(book.getId());
            assertThat(found.getTitle()).isEqualTo("Added by Alice");

            var updated = bookService.update(new BookUpdateDto(book.getId(), "Fixed by admin", 1L, Set.of(1L)));
            assertThat(updated.getTitle()).isEqualTo("Fixed by admin");

            bookService.deleteById(book.getId());
            return null;
        });
    }

    @DisplayName("findAll должен возвращать пользователю только его книги, а админу — все")
    @Test
    void findAllReturnsOnlyOwnBooksForRegularUserButAllForAdmin() {
        var aliceBook = runAs("alice", "ROLE_USER",
                () -> bookService.insert(new BookCreateDto("Alice's book", 1L, Set.of(1L))));
        var bobBook = runAs("bob", "ROLE_USER",
                () -> bookService.insert(new BookCreateDto("Bob's book", 2L, Set.of(2L))));

        var aliceView = runAs("alice", "ROLE_USER", () -> bookService.findAll());
        assertThat(aliceView).extracting(BookDto::getId)
                .contains(aliceBook.getId())
                .doesNotContain(bobBook.getId());

        var bobView = runAs("bob", "ROLE_USER", () -> bookService.findAll());
        assertThat(bobView).extracting(BookDto::getId)
                .contains(bobBook.getId())
                .doesNotContain(aliceBook.getId());

        var adminView = runAs("admin", "ROLE_ADMIN", () -> bookService.findAll());
        assertThat(adminView).extracting(BookDto::getId)
                .contains(aliceBook.getId(), bobBook.getId());
    }

    @DisplayName("обычный пользователь не должен иметь доступа к книге без владельца")
    @Test
    void regularUserCannotAccessOwnerlessBook() {
        runAs("bob", "ROLE_USER", () -> {
            assertThatThrownBy(() -> bookService.findById(1L))
                    .isInstanceOf(AccessDeniedException.class);
            assertThatThrownBy(() -> bookService.update(new BookUpdateDto(1L, "Hacked", 1L, Set.of(1L))))
                    .isInstanceOf(AccessDeniedException.class);
            assertThatThrownBy(() -> bookService.deleteById(1L))
                    .isInstanceOf(AccessDeniedException.class);
            return null;
        });
    }

    @DisplayName("администратор должен иметь доступ к книге без владельца")
    @Test
    void adminCanAccessOwnerlessBook() {
        runAs("admin", "ROLE_ADMIN", () -> {
            var found = bookService.findById(1L);
            assertThat(found.getId()).isEqualTo(1L);

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
