package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Student;
import ru.otus.hw.security.LoginContext;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final LocalizedIOService ioService;

    private final LoginContext loginContext;

    @Override
    public Student determineCurrentStudent() {
        return loginContext.getStudent();
    }

    @Override
    public Student createStudent() {
        var firstName = ioService.readStringWithPromptLocalized("StudentService.input.first.name");
        var lastName = ioService.readStringWithPromptLocalized("StudentService.input.last.name");

        return new Student(firstName, lastName);
    }
}
