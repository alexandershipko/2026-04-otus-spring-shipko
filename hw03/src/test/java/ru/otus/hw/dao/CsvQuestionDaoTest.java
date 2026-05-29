package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.service.LocalizedMessagesService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class CsvQuestionDaoTest {

    @MockitoBean(name = "localizedMessagesServiceImpl")
    private LocalizedMessagesService localizedMessagesService;

    @Autowired
    private CsvQuestionDao csvQuestionDao;

    @Test
    @DisplayName("load questions from CSV file for configured locale")
    void shouldLoadQuestionsFromExistingFile() {
        List<Question> questions = csvQuestionDao.findAll();

        assertThat(questions).isNotNull();
        assertThat(questions).isNotEmpty();
        assertThat(questions).hasSize(3);
    }

    @Test
    @DisplayName("parse first question correctly")
    void shouldParseFirstQuestionCorrectly() {
        List<Question> questions = csvQuestionDao.findAll();

        Question firstQuestion = questions.get(0);
        assertThat(firstQuestion.text()).isEqualTo("Is there life on Mars?");

        List<Answer> answers = firstQuestion.answers();
        assertThat(answers).hasSize(3);
        assertThat(answers.get(0).text()).isEqualTo("Science doesn't know this yet");
        assertThat(answers.get(0).isCorrect()).isTrue();
        assertThat(answers.get(1).text()).contains("UFO");
        assertThat(answers.get(1).isCorrect()).isFalse();
    }

    @Test
    @DisplayName("throw QuestionReadException when file not found")
    void shouldThrowExceptionWhenFileNotFound() {
        when(localizedMessagesService.getMessage(anyString(), any())).thenReturn("file not found");
        CsvQuestionDao daoWithMissingFile =
                new CsvQuestionDao(() -> "non_existent_file.csv", localizedMessagesService);

        assertThatThrownBy(daoWithMissingFile::findAll)
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("file not found");
    }

}
