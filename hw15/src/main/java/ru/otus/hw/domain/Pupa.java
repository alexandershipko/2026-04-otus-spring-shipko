package ru.otus.hw.domain;

public record Pupa(long id, String species) {

    public Stage stage() {
        return Stage.PUPA;
    }
}
