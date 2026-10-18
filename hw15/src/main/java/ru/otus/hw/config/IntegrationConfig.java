package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.dsl.MessageChannelSpec;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import ru.otus.hw.domain.Caterpillar;
import ru.otus.hw.service.EmergenceService;
import ru.otus.hw.service.HatchService;
import ru.otus.hw.service.LarvaService;
import ru.otus.hw.service.PupationService;

@Configuration
public class IntegrationConfig {

    // Последний возраст гусеницы (instar), после которого она окукливается
    private static final int MAX_INSTAR = 5;

    @Bean
    public MessageChannelSpec<?, ?> eggsChannel() {
        return MessageChannels.queue(10);
    }

    @Bean
    public MessageChannelSpec<?, ?> butterfliesChannel() {
        return MessageChannels.publishSubscribe();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerSpec poller() {
        return Pollers.fixedRate(100).maxMessagesPerPoll(2);
    }

    @Bean
    public IntegrationFlow metamorphosisFlow(HatchService hatchService,
                                             PupationService pupationService,
                                             EmergenceService emergenceService) {
        return IntegrationFlow.from(eggsChannel())
                .split()
                .handle(hatchService, "hatch")
                .gateway("larvaFlow.input")
                .handle(pupationService, "pupate")
                .handle(emergenceService, "emerge")
                .aggregate()
                .channel(butterfliesChannel())
                .get();
    }

    @Bean
    public IntegrationFlow larvaFlow(LarvaService larvaService) {
        return f -> f
                .handle(larvaService, "feed")
                .handle(larvaService, "molt")
                .<Caterpillar, Boolean>route(c -> c.instar() < MAX_INSTAR, m -> m
                        .subFlowMapping(true, sf -> sf.channel("larvaFlow.input"))
                        .subFlowMapping(false, IntegrationFlowDefinition::bridge));
    }
}
