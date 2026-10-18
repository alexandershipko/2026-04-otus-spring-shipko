package ru.otus.hw;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.domain.Butterfly;
import ru.otus.hw.domain.Egg;
import ru.otus.hw.domain.Stage;
import ru.otus.hw.service.MetamorphosisGateway;
import ru.otus.hw.service.NatureService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MetamorphosisFlowTest {

    @MockitoBean
    private NatureService natureService;

    @Autowired
    private MetamorphosisGateway metamorphosis;

    @Test
    @DisplayName("should turn every egg of the clutch into an adult butterfly")
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
}
