package ru.otus.hw.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Caterpillar;
import ru.otus.hw.domain.Pupa;
import ru.otus.hw.service.PupationService;

@Slf4j
@Service
public class PupationServiceImpl implements PupationService {

    @Override
    public Pupa pupate(Caterpillar caterpillar) {
        log.info("#{} {}: {} (instar {}) -> pupating", caterpillar.id(), caterpillar.species(),
                caterpillar.stage().getDescription(), caterpillar.instar());

        return new Pupa(caterpillar.id(), caterpillar.species());
    }
}
