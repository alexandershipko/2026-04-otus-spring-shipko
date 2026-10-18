package ru.otus.hw.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Butterfly;
import ru.otus.hw.domain.Egg;
import ru.otus.hw.service.MetamorphosisGateway;
import ru.otus.hw.service.NatureService;

@Slf4j
@Service
@RequiredArgsConstructor
public class NatureServiceImpl implements NatureService {

    private static final String[] SPECIES = {"Monarch", "Swallowtail", "Peacock", "Red Admiral", "Brimstone"};

    private static final int CLUTCHES_COUNT = 10;

    private static final int MAX_EGGS_IN_CLUTCH = 4;

    private static final long DELAY_MS = 1000;

    private final MetamorphosisGateway metamorphosis;

    private final AtomicLong eggIdSequence = new AtomicLong();

    @Override
    public void startEggLayingLoop() {
        ForkJoinPool pool = ForkJoinPool.commonPool();

        for (int i = 0; i < CLUTCHES_COUNT; i++) {
            int num = i + 1;
            pool.execute(() -> {
                Collection<Egg> eggs = layEggs();
                log.info("Clutch {}, laid eggs: {}", num, eggs);
                Collection<Butterfly> butterflies = metamorphosis.process(eggs);
                log.info("Clutch {}, emerged butterflies: {}", num, butterflies);
            });
            delay();
        }
    }

    private Collection<Egg> layEggs() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int count = random.nextInt(1, MAX_EGGS_IN_CLUTCH + 1);
        List<Egg> eggs = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String species = SPECIES[random.nextInt(SPECIES.length)];
            eggs.add(new Egg(eggIdSequence.incrementAndGet(), species));
        }

        return eggs;
    }

    private static void delay() {
        try {
            Thread.sleep(DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
