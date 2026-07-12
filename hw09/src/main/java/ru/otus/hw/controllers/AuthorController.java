package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

@RequiredArgsConstructor
@Controller
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping("/authors")
    public String findAll(Model model) {
        var authors = authorService.findAll().stream()
                .map(author -> new AuthorDto(author.getId(), author.getFullName()))
                .toList();
        model.addAttribute("authors", authors);

        return "authors/list";
    }

}
