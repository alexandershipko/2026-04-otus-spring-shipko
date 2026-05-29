package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.service.LocalizedMessagesService;

import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CsvQuestionDao implements QuestionDao {

    public static final char SEPARATOR = ';';

    public static final int SKIP_LINES = 1;

    private final TestFileNameProvider fileNameProvider;

    private final LocalizedMessagesService localizedMessagesService;

    public CsvQuestionDao(TestFileNameProvider fileNameProvider,
                          @Qualifier("localizedMessagesServiceImpl")
                          LocalizedMessagesService localizedMessagesService) {
        this.fileNameProvider = fileNameProvider;
        this.localizedMessagesService = localizedMessagesService;
    }

    @Override
    public List<Question> findAll() {
        String fileName = fileNameProvider.getTestFileName();

        try (var inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new QuestionReadException(
                        localizedMessagesService.getMessage("Error.file.not.found.in.resources", fileName));
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
            throw new QuestionReadException(
                    localizedMessagesService.getMessage("Error.failed.to.parse.csv.file", fileName), e);
        }
    }
}
