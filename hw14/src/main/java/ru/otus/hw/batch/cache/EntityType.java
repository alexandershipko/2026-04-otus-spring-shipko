package ru.otus.hw.batch.cache;

public enum EntityType {

    AUTHOR("author"),
    GENRE("genre"),
    BOOK("book"),
    COMMENT("comment");

    private final String code;

    EntityType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

}
