package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

import static org.mockito.BDDMockito.given;

@DisplayName("REST-контроллер авторов")
@WebFluxTest(AuthorController.class)
class AuthorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthorService authorService;

    @DisplayName("должен возвращать список авторов")
    @Test
    void shouldReturnAllAuthors() {
        given(authorService.findAll()).willReturn(Flux.just(new AuthorDto(1, "Author_1")));

        webTestClient.get().uri("/api/authors")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].fullName").isEqualTo("Author_1");
    }

}
