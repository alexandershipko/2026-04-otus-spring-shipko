package ru.otus.hw.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    private TestServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new TestServiceImpl(ioService, questionDao);
    }

    @Test
    void executeTestShouldPrintQuestionsWithNumberedAnswers() {
        List<Answer> answers = List.of(
                new Answer("Paris", true),
                new Answer("London", false),
                new Answer("Berlin", false));

        List<Question> questions = List.of(
                new Question("What is the capital of France?", answers));

        when(questionDao.findAll()).thenReturn(questions);

        testService.executeTest();

        verify(ioService).printFormattedLine("%nPlease answer the questions below%n");
        verify(ioService).printFormattedLine("%d. %s", 1, "What is the capital of France?");
        verify(ioService).printFormattedLine("   %d) %s", 1, "Paris");
        verify(ioService).printFormattedLine("   %d) %s", 2, "London");
        verify(ioService).printFormattedLine("   %d) %s", 3, "Berlin");

        assertThat(questions).hasSize(1);
        assertThat(questions.getFirst().answers()).hasSize(3);
    }

}
