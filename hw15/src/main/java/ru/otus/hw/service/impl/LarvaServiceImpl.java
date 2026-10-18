package ru.otus.hw.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Caterpillar;
import ru.otus.hw.service.LarvaService;

@Slf4j
@Service
public class LarvaServiceImpl implements LarvaService {

    @Override
    public Caterpillar feed(Caterpillar caterpillar) {
        log.info("#{} {}: {} (instar {}) -> feeding", caterpillar.id(), caterpillar.species(),
                caterpillar.stage().getDescription(), caterpillar.instar());

        return caterpillar;
    }

    @Override
    public Caterpillar molt(Caterpillar caterpillar) {
        int nextInstar = caterpillar.instar() + 1;

        log.info("#{} {}: {} (instar {}) -> molting to instar {}", caterpillar.id(), caterpillar.species(),
                caterpillar.stage().getDescription(), caterpillar.instar(), nextInstar);

        return new Caterpillar(caterpillar.id(), caterpillar.species(), nextInstar);
    }
}
