package ru.otus.hw.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Caterpillar;
import ru.otus.hw.domain.Egg;
import ru.otus.hw.service.HatchService;

@Slf4j
@Service
public class HatchServiceImpl implements HatchService {

    @Override
    public Caterpillar hatch(Egg egg) {
        log.info("#{} {}: {} -> hatching", egg.id(), egg.species(), egg.stage().getDescription());

        return new Caterpillar(egg.id(), egg.species(), 1);
    }
}
