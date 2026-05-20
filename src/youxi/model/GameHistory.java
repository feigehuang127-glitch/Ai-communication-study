package youxi.model;

public class GameHistory {
    private int id;
    private int userId;
    private String category;
    private String result;          // "win_combo" / "win" / "lose"
    private int correctCount;
    private int totalTimeSeconds;
    private int scoreEarned;
    private String rankBefore;
    private String rankAfter;
    private String playedAt;

    public GameHistory() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
    public int getTotalTimeSeconds() { return totalTimeSeconds; }
    public void setTotalTimeSeconds(int totalTimeSeconds) { this.totalTimeSeconds = totalTimeSeconds; }
    public int getScoreEarned() { return scoreEarned; }
    public void setScoreEarned(int scoreEarned) { this.scoreEarned = scoreEarned; }
    public String getRankBefore() { return rankBefore; }
    public void setRankBefore(String rankBefore) { this.rankBefore = rankBefore; }
    public String getRankAfter() { return rankAfter; }
    public void setRankAfter(String rankAfter) { this.rankAfter = rankAfter; }
    public String getPlayedAt() { return playedAt; }
    public void setPlayedAt(String playedAt) { this.playedAt = playedAt; }
}
