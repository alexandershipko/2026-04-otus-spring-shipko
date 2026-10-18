package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.otus.hw.service.NatureService;

@Component
@RequiredArgsConstructor
public class AppRunner implements CommandLineRunner {

    private final NatureService natureService;

    @Override
    public void run(String... args) {
        natureService.startEggLayingLoop();
    }
}
