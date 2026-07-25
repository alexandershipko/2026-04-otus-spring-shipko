package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("authors")
@EqualsAndHashCode(of = "id")
public class Author {

    @Id
    private long id;

    @Column("full_name")
    private String fullName;

}
