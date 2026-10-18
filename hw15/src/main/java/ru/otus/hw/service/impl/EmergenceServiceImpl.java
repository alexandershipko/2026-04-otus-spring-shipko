package ru.otus.hw.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Butterfly;
import ru.otus.hw.domain.Pupa;
import ru.otus.hw.service.EmergenceService;

@Slf4j
@Service
public class EmergenceServiceImpl implements EmergenceService {

    @Override
    public Butterfly emerge(Pupa pupa) {
        log.info("#{} {}: {} -> emerging", pupa.id(), pupa.species(), pupa.stage().getDescription());

        return new Butterfly(pupa.id(), pupa.species());
    }
}
