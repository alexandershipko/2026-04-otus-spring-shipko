package ru.otus.hw.service;

import ru.otus.hw.domain.Caterpillar;
import ru.otus.hw.domain.Egg;

public interface HatchService {

    // Выводит гусеницу из яйца
    Caterpillar hatch(Egg egg);
}
