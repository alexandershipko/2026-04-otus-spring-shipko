package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

import static org.mockito.BDDMockito.given;

@DisplayName("REST-контроллер жанров")
@WebFluxTest(GenreController.class)
class GenreControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GenreService genreService;

    @DisplayName("должен возвращать список жанров")
    @Test
    void shouldReturnAllGenres() {
        given(genreService.findAll()).willReturn(Flux.just(new GenreDto(1, "Genre_1")));

        webTestClient.get().uri("/api/genres")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Genre_1");
    }

}
