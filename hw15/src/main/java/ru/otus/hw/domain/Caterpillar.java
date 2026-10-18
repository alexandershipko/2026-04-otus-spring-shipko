package ru.otus.hw.domain;

public record Caterpillar(long id, String species, int instar) {

    public Stage stage() {
        return Stage.LARVA;
    }
}
