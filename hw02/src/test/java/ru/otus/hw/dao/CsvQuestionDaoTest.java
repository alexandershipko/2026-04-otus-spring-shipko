package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestPropertySource("classpath:application.properties")
@ExtendWith(SpringExtension.class)
class CsvQuestionDaoTest {

    @Value("${test.fileName}")
    private String testFileName;

    private CsvQuestionDao csvQuestionDao;

    @BeforeEach
    void setUp() {
        TestFileNameProvider fileNameProvider = () -> testFileName;
        csvQuestionDao = new CsvQuestionDao(fileNameProvider);
    }

    @Test
    @DisplayName("load questions from existing CSV file")
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
        TestFileNameProvider missingFileProvider = () -> "non_existent_file.csv";
        csvQuestionDao = new CsvQuestionDao(missingFileProvider);

        assertThatThrownBy(csvQuestionDao::findAll)
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("Failed to parse CSV file: non_existent_file.csv");
    }

}
