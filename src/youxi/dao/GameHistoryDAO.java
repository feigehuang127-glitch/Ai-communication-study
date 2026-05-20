package youxi.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import youxi.model.GameHistory;
import youxi.util.DBHelper;

public class GameHistoryDAO {

    public void insert(GameHistory gh) throws SQLException {
        String sql = "INSERT INTO game_history (user_id, category, result, correct_count, " +
                     "total_time_seconds, score_earned, rank_before, rank_after) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gh.getUserId());
            ps.setString(2, gh.getCategory());
            ps.setString(3, gh.getResult());
            ps.setInt(4, gh.getCorrectCount());
            ps.setInt(5, gh.getTotalTimeSeconds());
            ps.setInt(6, gh.getScoreEarned());
            ps.setString(7, gh.getRankBefore());
            ps.setString(8, gh.getRankAfter());
            ps.executeUpdate();
        }
    }

    public List<GameHistory> findByUserId(int userId, int limit) throws SQLException {
        String sql = "SELECT * FROM game_history WHERE user_id = ? ORDER BY played_at DESC LIMIT ?";
        List<GameHistory> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rowToGameHistory(rs));
            }
        }
        return list;
    }

    public float getRecentAccuracy(int userId, int limit) throws SQLException {
        String sql = "SELECT correct_count FROM game_history WHERE user_id = ? ORDER BY played_at DESC LIMIT ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                int totalCorrect = 0;
                int gameCount = 0;
                while (rs.next()) {
                    totalCorrect += rs.getInt("correct_count");
                    gameCount++;
                }
                if (gameCount == 0) return 0.5f;
                int totalQuestions = gameCount * youxi.util.Config.questionsPerRound();
                return (float) totalCorrect / totalQuestions;
            }
        }
    }

    public GameStats getStats(int userId) throws SQLException {
        String sql = "SELECT " +
                     "COUNT(*) AS total_games, " +
                     "SUM(CASE WHEN result IN ('win','win_combo') THEN 1 ELSE 0 END) AS wins, " +
                     "SUM(CASE WHEN result = 'win_combo' THEN 1 ELSE 0 END) AS combo_wins, " +
                     "SUM(CASE WHEN result = 'lose' THEN 1 ELSE 0 END) AS losses, " +
                     "SUM(score_earned) AS total_score_earned " +
                     "FROM game_history WHERE user_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GameStats(
                        rs.getInt("total_games"),
                        rs.getInt("wins"),
                        rs.getInt("combo_wins"),
                        rs.getInt("losses"),
                        rs.getInt("total_score_earned")
                    );
                }
            }
        }
        return new GameStats(0, 0, 0, 0, 0);
    }

    public static class GameStats {
        public final int totalGames;
        public final int wins;
        public final int comboWins;
        public final int losses;
        public final int totalScoreEarned;

        public GameStats(int totalGames, int wins, int comboWins, int losses, int totalScoreEarned) {
            this.totalGames = totalGames;
            this.wins = wins;
            this.comboWins = comboWins;
            this.losses = losses;
            this.totalScoreEarned = totalScoreEarned;
        }
    }

    private GameHistory rowToGameHistory(ResultSet rs) throws SQLException {
        GameHistory gh = new GameHistory();
        gh.setId(rs.getInt("id"));
        gh.setUserId(rs.getInt("user_id"));
        gh.setCategory(rs.getString("category"));
        gh.setResult(rs.getString("result"));
        gh.setCorrectCount(rs.getInt("correct_count"));
        gh.setTotalTimeSeconds(rs.getInt("total_time_seconds"));
        gh.setScoreEarned(rs.getInt("score_earned"));
        gh.setRankBefore(rs.getString("rank_before"));
        gh.setRankAfter(rs.getString("rank_after"));
        gh.setPlayedAt(rs.getString("played_at"));
        return gh;
    }
}
