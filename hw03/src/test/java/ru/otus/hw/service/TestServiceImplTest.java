package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private LocalizedIOService ioService;

    @Mock
    private QuestionDao questionDao;

    private TestServiceImpl testService;

    private Student student;

    @BeforeEach
    void setUp() {
        testService = new TestServiceImpl(ioService, questionDao);
        student = new Student("Ivan", "Ivanov");
    }

    @Test
    @DisplayName("execute test and return correct result when user answers correctly")
    void executeTestShouldReturnCorrectTestResult() {
        List<Answer> answers = List.of(
                new Answer("Paris", true),
                new Answer("London", false),
                new Answer("Berlin", false));

        List<Question> questions = List.of(
                new Question("What is the capital of France?", answers));

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPromptLocalized(eq(1), eq(3), anyString(), anyString()))
                .thenReturn(1);

        TestResult result = testService.executeTestFor(student);

        verify(ioService).printLineLocalized("TestService.answer.the.questions");
        verify(ioService).printFormattedLine("%d. %s", 1, "What is the capital of France?");
        verify(ioService).printFormattedLine("   %d) %s", 1, "Paris");
        verify(ioService).printFormattedLine("   %d) %s", 2, "London");
        verify(ioService).printFormattedLine("   %d) %s", 3, "Berlin");
        verify(ioService).readIntForRangeWithPromptLocalized(1, 3,
                "TestService.enter.the.answer.number",
                "TestService.error.during.reading.int.value.try.again");

        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getAnsweredQuestions()).hasSize(1);
        assertThat(result.getRightAnswersCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("execute test and return correct result when user answers incorrectly")
    void executeTestShouldReturnTestResultWithWrongAnswer() {
        List<Answer> answers = List.of(
                new Answer("Paris", true),
                new Answer("London", false),
                new Answer("Berlin", false));

        List<Question> questions = List.of(
                new Question("What is the capital of France?", answers));

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPromptLocalized(eq(1), eq(3), anyString(), anyString()))
                .thenReturn(2);

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getRightAnswersCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("handle multiple questions correctly")
    void executeTestShouldHandleMultipleQuestions() {
        List<Answer> answers1 = List.of(
                new Answer("Paris", true),
                new Answer("London", false));

        List<Answer> answers2 = List.of(
                new Answer("True", true),
                new Answer("False", false));

        List<Question> questions = List.of(
                new Question("What is the capital of France?", answers1),
                new Question("Java is a programming language?", answers2));

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPromptLocalized(eq(1), eq(2), anyString(), anyString()))
                .thenReturn(1, 1);

        TestResult result = testService.executeTestFor(student);

        verify(ioService, times(2))
                .readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString());

        assertThat(result.getAnsweredQuestions()).hasSize(2);
        assertThat(result.getRightAnswersCount()).isEqualTo(2);
    }

}
