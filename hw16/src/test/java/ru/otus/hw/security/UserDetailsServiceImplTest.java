package ru.otus.hw.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Собственная реализация UserDetailsService")
@DataJpaTest
@Import(UserDetailsServiceImpl.class)
class UserDetailsServiceImplTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @DisplayName("должен загружать пользователя по имени")
    @Test
    void shouldLoadUserByUsername() {
        var userDetails = userDetailsService.loadUserByUsername("testuser");

        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword())
                .isEqualTo("$2a$10$3CD.NfzIHU./G7SorJ8TIuaZwB09Q9MQm9v2nacZn/qat/VaExOdG");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @DisplayName("должен выбрасывать исключение для несуществующего пользователя")
    @Test
    void shouldThrowExceptionForMissingUser() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

}
