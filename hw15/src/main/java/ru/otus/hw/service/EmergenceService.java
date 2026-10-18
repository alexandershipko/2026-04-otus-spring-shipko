package ru.otus.hw.service;

import ru.otus.hw.domain.Butterfly;
import ru.otus.hw.domain.Pupa;

public interface EmergenceService {

    // Превращает куколку в бабочку
    Butterfly emerge(Pupa pupa);
}
