package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.logging.TrackTime;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @Override
    @TrackTime
    public List<Author> findAll() {
        return authorRepository.findAll();
    }

}
