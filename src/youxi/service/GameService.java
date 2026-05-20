package youxi.service;

import java.sql.SQLException;
import java.util.*;

import youxi.dao.QuestionDAO;
import youxi.model.Question;
import youxi.model.User;
import youxi.util.Config;

public class GameService {

    public List<Question> generateQuestions(User user, String category, float accuracy) {
        int[] range = UserService.getDifficultyRange(user.getRank());
        int minD = applyPreference(range[0]);
        int maxD = Math.max(minD, applyPreference(range[1]));
        int span = maxD - minD;
        float offset;
        if (accuracy >= 0.90f) offset = 1.0f;
        else if (accuracy >= 0.80f) offset = 0.75f;
        else if (accuracy >= 0.70f) offset = 0.50f;
        else if (accuracy >= 0.60f) offset = 0.25f;
        else if (accuracy >= 0.50f) offset = 0.05f;
        else offset = 0.0f;
        int effectiveMax = minD + Math.round(span * offset);
        if (effectiveMax < minD) effectiveMax = minD;

        int needed = Config.questionsPerRound();
        QuestionCache cache = QuestionCache.getInstance();
        return cache.pickRandom(category, minD, effectiveMax, needed);
    }

    public List<Question> generateDailyQuestions(User user) {
        int[] range = UserService.getDifficultyRange(user.getRank());
        int minD = applyPreference(range[0]);
        int maxD = Math.max(minD, applyPreference(range[1]));
        int needed = Config.questionsPerRound();
        long seed = java.time.LocalDate.now().toString().hashCode();
        QuestionCache cache = QuestionCache.getInstance();

        List<Question> pool = new ArrayList<>();
        for (String cat : cache.getCategories()) {
            List<Question> catPool = cache.getByCategoryAndDifficulty(cat, minD, maxD);
            pool.addAll(catPool);
        }
        if (pool.size() < needed) {
            for (String cat : cache.getCategories()) {
                pool.addAll(cache.getByCategory(cat));
            }
        }
        pool.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        Collections.shuffle(pool, new Random(seed));
        return pool.subList(0, Math.min(needed, pool.size()));
    }

    private int applyPreference(int difficulty) {
        int pref = Config.difficultyPreference();
        int d = difficulty + pref;
        if (d < 1) d = 1;
        if (d > 5) d = 5;
        return d;
    }

    public boolean isCorrect(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.isEmpty()) return false;
        String normalizedUser = sortAnswer(userAnswer);
        String correctAnswer = question.getAnswer();
        return normalizedUser.equalsIgnoreCase(correctAnswer);
    }

    public String sortAnswer(String answer) {
        char[] chars = answer.replace(",", "").trim().toUpperCase().toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    public boolean isComboWin(int comboCount) {
        return comboCount >= Config.comboThreshold();
    }

    public int calculateScore(int correctCount, int totalQuestions, boolean comboWin, String rank,
                              int speedBonus, int comboBonus, int allCorrectBonus) {
        int base = correctCount * Config.baseScore();
        if (comboWin) base += Config.baseScore() * 3;
        int total = base + speedBonus + comboBonus + allCorrectBonus;
        int rankIndex = UserService.getRankIndex(rank);
        if (rankIndex <= 0) total = (int)(total * 1.5);
        else if (rankIndex <= 1) total = (int)(total * 1.3);
        else if (rankIndex <= 2) total = (int)(total * 1.1);
        return total;
    }
}
