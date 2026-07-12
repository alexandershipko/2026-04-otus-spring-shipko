package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

@RequiredArgsConstructor
@Controller
public class GenreController {

    private final GenreService genreService;

    @GetMapping("/genres")
    public String findAll(Model model) {
        var genres = genreService.findAll().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .toList();
        model.addAttribute("genres", genres);

        return "genres/list";
    }

}
