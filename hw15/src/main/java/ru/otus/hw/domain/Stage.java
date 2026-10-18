package ru.otus.hw.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Stage {
    EGG("Egg"),
    LARVA("Larva"),
    PUPA("Pupa"),
    ADULT("Adult");

    private final String description;
}
