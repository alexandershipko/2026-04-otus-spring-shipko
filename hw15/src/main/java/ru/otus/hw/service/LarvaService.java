package ru.otus.hw.service;

import ru.otus.hw.domain.Caterpillar;

public interface LarvaService {

    // Кормит гусеницу в текущем возрасте (instar)
    Caterpillar feed(Caterpillar caterpillar);

    // Проводит линьку гусеницы и переводит её в следующий возраст (instar)
    Caterpillar molt(Caterpillar caterpillar);
}
