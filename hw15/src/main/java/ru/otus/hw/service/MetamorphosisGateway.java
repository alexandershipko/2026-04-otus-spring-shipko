package ru.otus.hw.service;

import java.util.Collection;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.domain.Butterfly;
import ru.otus.hw.domain.Egg;

@MessagingGateway
public interface MetamorphosisGateway {

    // Отправляет яйца на метаморфоз и возвращает появившихся бабочек
    @Gateway(requestChannel = "eggsChannel", replyChannel = "butterfliesChannel")
    Collection<Butterfly> process(Collection<Egg> eggs);
}
