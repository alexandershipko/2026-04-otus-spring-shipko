package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {
    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printFormattedLine("%nPlease answer the questions below%n");

        int i = 1;
        for (Question q : questionDao.findAll()) {
            ioService.printFormattedLine("%d. %s", i++, q.text());

            List<Answer> answers = q.answers();

            for (int j = 0; j < answers.size(); j++) {
                ioService.printFormattedLine("   %d) %s", j + 1, answers.get(j).text());
            }

            ioService.printLine("");
        }
    }

}
