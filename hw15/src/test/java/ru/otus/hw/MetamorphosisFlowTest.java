package ru.otus.hw;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.otus.hw.domain.Butterfly;
import ru.otus.hw.domain.Egg;
import ru.otus.hw.domain.Stage;
import ru.otus.hw.service.MetamorphosisGateway;
import ru.otus.hw.service.NatureService;
import ru.otus.hw.service.PupationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class MetamorphosisFlowTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @MockitoBean
    private NatureService natureService;

    @MockitoSpyBean
    private PupationService pupationService;

    @Autowired
    private MetamorphosisGateway metamorphosis;

    @Test
    @DisplayName("должен превращать каждое яйцо кладки во взрослую бабочку")
    void shouldTurnEggsIntoButterflies() {
        List<Egg> eggs = List.of(
                new Egg(1, "Monarch"),
                new Egg(2, "Peacock"),
                new Egg(3, "Brimstone"));

        Collection<Butterfly> butterflies = metamorphosis.process(eggs);

        assertThat(butterflies)
                .containsExactly(
                        new Butterfly(1, "Monarch"),
                        new Butterfly(2, "Peacock"),
                        new Butterfly(3, "Brimstone"))
                .allMatch(butterfly -> butterfly.stage() == Stage.ADULT);
    }

    @Test
    @DisplayName("должен пробрасывать исключение из потока в код, вызвавший gateway")
    void shouldPropagateExceptionFromFlow() {
        doThrow(new IllegalStateException("pupation failed"))
                .when(pupationService).pupate(argThat(caterpillar -> caterpillar.id() == 2));

        List<Egg> eggs = List.of(
                new Egg(1, "Monarch"),
                new Egg(2, "Peacock"));

        assertTimeoutPreemptively(TIMEOUT, () ->
                assertThatThrownBy(() -> metamorphosis.process(eggs))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("pupation failed"));
    }

    @Test
    @DisplayName("должен возвращать пустой результат для пустой кладки")
    void shouldReturnNoButterfliesForEmptyClutch() {
        Collection<Butterfly> butterflies = assertTimeoutPreemptively(TIMEOUT,
                () -> metamorphosis.process(List.of()));

        assertThat(butterflies).isEmpty();
    }
}
