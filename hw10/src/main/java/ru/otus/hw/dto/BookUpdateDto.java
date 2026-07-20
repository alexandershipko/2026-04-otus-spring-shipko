package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookUpdateDto {

    private long id;

    @NotBlank(message = "{books.form.validation.title.notBlank}")
    private String title;

    @NotNull(message = "{books.form.validation.author.notNull}")
    private Long authorId;

    @NotEmpty(message = "{books.form.validation.genres.notEmpty}")
    private Set<Long> genreIds;

}
