package com.platform.dto;

import com.platform.model.Question;
import java.util.List;
import java.util.Map;

public class GameResultResponse {
    private String result;
    private int correctCount;
    private int totalQuestions;
    private int scoreEarned;
    private Map<Integer, Boolean> answerResults;
    private List<Question> questions;

    public GameResultResponse(String result, int correctCount, int totalQuestions,
                              int scoreEarned, Map<Integer, Boolean> answerResults,
                              List<Question> questions) {
        this.result = result;
        this.correctCount = correctCount;
        this.totalQuestions = totalQuestions;
        this.scoreEarned = scoreEarned;
        this.answerResults = answerResults;
        this.questions = questions;
    }

    public String getResult() { return result; }
    public int getCorrectCount() { return correctCount; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getScoreEarned() { return scoreEarned; }
    public Map<Integer, Boolean> getAnswerResults() { return answerResults; }
    public List<Question> getQuestions() { return questions; }
}
