package youxi.dao;

import java.sql.*;
import java.time.LocalDate;

import youxi.util.DBHelper;

public class DailyChallengeDAO {

    public void ensureTable() {
        String sql = "CREATE TABLE IF NOT EXISTS daily_challenges (" +
                     "id INT PRIMARY KEY AUTO_INCREMENT, " +
                     "user_id INT NOT NULL, " +
                     "challenge_date DATE NOT NULL, " +
                     "score INT NOT NULL DEFAULT 0, " +
                     "completed TINYINT NOT NULL DEFAULT 0, " +
                     "played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                     "UNIQUE KEY uk_user_date (user_id, challenge_date)" +
                     ")";
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasCompletedToday(int userId) throws SQLException {
        String today = LocalDate.now().toString();
        String sql = "SELECT completed FROM daily_challenges WHERE user_id = ? AND challenge_date = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, today);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("completed") == 1;
            }
        }
    }

    public void record(int userId, int score, boolean completed) throws SQLException {
        String today = LocalDate.now().toString();
        String sql = "INSERT INTO daily_challenges (user_id, challenge_date, score, completed) " +
                     "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE score = VALUES(score), completed = VALUES(completed)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, today);
            ps.setInt(3, score);
            ps.setInt(4, completed ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public int getStreak(int userId) throws SQLException {
        String sql = "SELECT challenge_date FROM daily_challenges WHERE user_id = ? AND completed = 1 ORDER BY challenge_date DESC";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                int streak = 0;
                LocalDate expected = LocalDate.now();
                while (rs.next()) {
                    LocalDate date = rs.getDate("challenge_date").toLocalDate();
                    if (date.equals(expected)) {
                        streak++;
                        expected = expected.minusDays(1);
                    } else if (date.equals(expected.minusDays(1))) {
                        expected = date.minusDays(1);
                    } else {
                        break;
                    }
                }
                return streak;
            }
        }
    }

    public int getTodayScore(int userId) throws SQLException {
        String today = LocalDate.now().toString();
        String sql = "SELECT score FROM daily_challenges WHERE user_id = ? AND challenge_date = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, today);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("score") : 0;
            }
        }
    }
}
