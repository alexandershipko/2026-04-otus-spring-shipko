package ru.otus.hw.domain;

public record Egg(long id, String species) {

    public Stage stage() {
        return Stage.EGG;
    }
}
