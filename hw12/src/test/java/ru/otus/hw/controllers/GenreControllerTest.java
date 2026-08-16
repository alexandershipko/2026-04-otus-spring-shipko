package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("Контроллер жанров")
@WebMvcTest(GenreController.class)
@Import(SecurityConfig.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreService genreService;

    @DisplayName("должен отображать список жанров аутентифицированному пользователю")
    @WithMockUser
    @Test
    void shouldReturnGenresList() throws Exception {
        given(genreService.findAll()).willReturn(List.of(new GenreDto(1, "Genre_1")));

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genres/list"))
                .andExpect(model().attribute("genres", List.of(new GenreDto(1, "Genre_1"))));
    }

    @DisplayName("должен перенаправлять неаутентифицированного пользователя на страницу логина")
    @Test
    void shouldRedirectAnonymousUserToLogin() throws Exception {
        mockMvc.perform(get("/genres"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

}
