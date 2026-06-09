package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import ru.otus.hw.security.LoginContext;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.StudentService;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent(value = "Application Events Commands")
@RequiredArgsConstructor
public class ApplicationEventsCommands {

    private final LoginContext loginContext;

    private final StudentService studentService;

    private final TestRunnerService testRunnerService;

    private final LocalizedIOService localizedMessagesService;

    @ShellMethod(value = "Login command", key = {"l", "login"})
    public String login() {
        var student = studentService.createStudent();
        loginContext.login(student);

        return localizedMessagesService.getMessage("ApplicationEventsCommands.hello",
                student.firstName(), student.lastName());
    }

    @ShellMethod(value = "Run test command", key = {"t", "test"})
    @ShellMethodAvailability("isTestCommandAvailable")
    public void runTest() {
        testRunnerService.run();
    }

    private Availability isTestCommandAvailable() {
        return loginContext.isStudentLoggedIn()
                ? Availability.available()
                : Availability.unavailable(
                        localizedMessagesService.getMessage("ApplicationEventsCommands.please.login.first"));
    }

}
