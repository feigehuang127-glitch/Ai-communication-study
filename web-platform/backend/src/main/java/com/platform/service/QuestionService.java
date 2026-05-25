package com.platform.service;

import com.platform.model.Question;
import com.platform.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> getQuestionsByCollege(String college) {
        return questionRepository.findByCollegeAndDifficultyBetween(college, 1, 10);
    }

    public List<Question> getQuestionsByCollegeAndCategory(String college, String category) {
        return questionRepository.findByCollegeAndCategory(college, category);
    }

    public long countByCollege(String college) {
        return questionRepository.countByCollege(college);
    }
}
