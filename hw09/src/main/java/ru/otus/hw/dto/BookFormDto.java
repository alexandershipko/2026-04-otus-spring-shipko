package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookFormDto {

    private long id;

    private String title;

    private Long authorId;

    private Set<Long> genreIds;

}
