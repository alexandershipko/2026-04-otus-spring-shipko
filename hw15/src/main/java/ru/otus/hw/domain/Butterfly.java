package ru.otus.hw.domain;

public record Butterfly(long id, String species) {

    public Stage stage() {
        return Stage.ADULT;
    }
}
