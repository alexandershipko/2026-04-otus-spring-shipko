package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCommentCreateDto {

    @NotBlank(message = "{books.view.validation.comment.notBlank}")
    private String text;

    private String bookId;

}
