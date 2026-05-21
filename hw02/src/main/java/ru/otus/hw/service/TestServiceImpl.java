package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

//    @Override
//    public TestResult executeTestFor(Student student) {
//        ioService.printLine("");
//        ioService.printFormattedLine("Please answer the questions below%n");
//        var questions = questionDao.findAll();
//        var testResult = new TestResult(student);
//
//        int i = 1;
//        for (var question: questions) {
//            ioService.printFormattedLine("%d. %s", i++, question.text());
//
//            List<Answer> answers = question.answers();
//
//            for (int j = 0; j < answers.size(); j++) {
//                ioService.printFormattedLine("   %d) %s", j + 1, answers.get(j).text());
//            }
//            int answerNum =  ioService.readIntForRangeWithPrompt(1,
//                    question.answers().size(),
//                    "Enter the answer number", "Error during reading int value, try again");
//            var isAnswerValid = question.answers().get(answerNum - 1).isCorrect();
//            testResult.applyAnswer(question, isAnswerValid);
//            ioService.printLine("");
//        }
//
//        return testResult;
//    }

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        if (questions == null || questions.isEmpty()) {
            ioService.printLine("No questions found.");
            return testResult;
        }

        int questionNumber = 1;
        for (Question question : questions) {
            askQuestion(questionNumber++, question, testResult);
        }

        return testResult;
    }

    private void askQuestion(int number, Question question, TestResult testResult) {
        ioService.printFormattedLine("%d. %s", number, question.text());

        var answers = question.answers();
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("   %d) %s", i + 1, answers.get(i).text());
        }

        int userAnswer = ioService.readIntForRangeWithPrompt(
                1, answers.size(),
                "Enter the answer number",
                "Error during reading int value, try again"
        );

        testResult.applyAnswer(question, answers.get(userAnswer - 1).isCorrect());
        ioService.printLine("");
    }

}
