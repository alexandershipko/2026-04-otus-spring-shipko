package ru.otus.hw.service;

import ru.otus.hw.domain.Caterpillar;
import ru.otus.hw.domain.Pupa;

public interface PupationService {

    // Превращает гусеницу в куколку
    Pupa pupate(Caterpillar caterpillar);
}
