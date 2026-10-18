package ru.otus.hw.config;

import java.util.Collection;

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

    // Максимальное число кладок, ожидающих обработки в очереди
    private static final int EGGS_QUEUE_CAPACITY = 10;

    // Интервал опроса очереди кладок поллером, мс
    private static final long POLLER_RATE_MS = 100;

    // Максимальное число сообщений, забираемых поллером за один опрос
    private static final int POLLER_MAX_MESSAGES = 2;

    // Время ожидания недостающих яиц кладки, после которого неполная группа удаляется из агрегатора, мс
    private static final long AGGREGATOR_GROUP_TIMEOUT_MS = 10_000;

    @Bean
    public MessageChannelSpec<?, ?> eggsChannel() {
        return MessageChannels.queue(EGGS_QUEUE_CAPACITY);
    }

    @Bean
    public MessageChannelSpec<?, ?> butterfliesChannel() {
        return MessageChannels.publishSubscribe();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerSpec poller() {
        return Pollers.fixedRate(POLLER_RATE_MS).maxMessagesPerPoll(POLLER_MAX_MESSAGES);
    }

    @Bean
    public IntegrationFlow metamorphosisFlow(HatchService hatchService,
                                             PupationService pupationService,
                                             EmergenceService emergenceService) {
        return IntegrationFlow.from(eggsChannel())
                // Пустую кладку сразу возвращаем как пустой результат, иначе агрегатор никогда не ответит
                .<Collection<?>, Boolean>route(Collection::isEmpty, m -> m
                        .subFlowMapping(true, sf -> sf.channel(butterfliesChannel()))
                        .defaultOutputToParentFlow())
                .split()
                .handle(hatchService, "hatch")
                .gateway("larvaFlow.input")
                .handle(pupationService, "pupate")
                .handle(emergenceService, "emerge")
                .aggregate(a -> a
                        .groupTimeout(AGGREGATOR_GROUP_TIMEOUT_MS)
                        .expireGroupsUponTimeout(true))
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
