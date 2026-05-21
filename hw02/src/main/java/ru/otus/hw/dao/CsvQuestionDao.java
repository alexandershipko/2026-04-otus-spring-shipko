package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {

    public static final char SEPARATOR = ';';

    public static final int SKIP_LINES = 1;

    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        String fileName = fileNameProvider.getTestFileName();

        try (var inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {

            if (inputStream == null) {
                throw new QuestionReadException("File not found in resources: " + fileName);
            }

            List<QuestionDto> dtos = new CsvToBeanBuilder<QuestionDto>(new InputStreamReader(inputStream))
                    .withType(QuestionDto.class)
                    .withSeparator(SEPARATOR)
                    .withSkipLines(SKIP_LINES)
                    .build()
                    .parse();

            return dtos.stream()
                    .map(dto -> new Question(dto.getText(), dto.getAnswers()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new QuestionReadException("Failed to parse CSV file: " + fileName, e);
        }
    }

}
