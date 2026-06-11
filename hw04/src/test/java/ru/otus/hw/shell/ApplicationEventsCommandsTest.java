package ru.otus.hw.shell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.domain.Student;
import ru.otus.hw.security.LoginContext;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.StudentService;
import ru.otus.hw.service.TestRunnerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class ApplicationEventsCommandsTest {

    @MockitoBean
    private LoginContext loginContext;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private TestRunnerService testRunnerService;

    @MockitoBean
    private LocalizedIOService localizedIOService;

    private ApplicationEventsCommands commands;

    @BeforeEach
    void setUp() {
        commands = new ApplicationEventsCommands(loginContext, studentService, testRunnerService, localizedIOService);
    }

    @Test
    @DisplayName("login should create student, save to context and return greeting")
    void loginShouldCreateStudentAndReturnGreeting() {
        var student = new Student("Ivan", "Ivanov");
        when(studentService.createStudent()).thenReturn(student);
        when(localizedIOService.getMessage("ApplicationEventsCommands.hello", "Ivan", "Ivanov"))
                .thenReturn("Hello, Ivan Ivanov!");

        var result = commands.login();

        verify(studentService).createStudent();
        verify(loginContext).login(student);
        assertThat(result).isEqualTo("Hello, Ivan Ivanov!");
    }

    @Test
    @DisplayName("runTest should delegate to test runner service")
    void runTestShouldDelegateToTestRunnerService() {
        commands.runTest();
        verify(testRunnerService).run();
    }
}