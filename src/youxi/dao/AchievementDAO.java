package youxi.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import youxi.model.Achievement;
import youxi.util.DBHelper;

public class AchievementDAO {

    public static void ensureTable() {
        String sql = "CREATE TABLE IF NOT EXISTS achievements (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "user_id INT NOT NULL, " +
            "badge_key VARCHAR(50) NOT NULL, " +
            "earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (user_id) REFERENCES users(id), " +
            "UNIQUE KEY uk_user_badge (user_id, badge_key), " +
            "INDEX idx_ach_user (user_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        try (Connection c = DBHelper.getConnection();
             Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException e) {
            System.err.println("[AchievementDAO] 建表失败: " + e.getMessage());
        }
    }

    public boolean hasBadge(int userId, String badgeKey) throws SQLException {
        String sql = "SELECT COUNT(*) FROM achievements WHERE user_id = ? AND badge_key = ?";
        try (Connection c = DBHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, badgeKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean award(int userId, String badgeKey) throws SQLException {
        if (hasBadge(userId, badgeKey)) return false;
        String sql = "INSERT INTO achievements (user_id, badge_key) VALUES (?, ?)";
        try (Connection c = DBHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, badgeKey);
            ps.executeUpdate();
            return true;
        }
    }

    public List<Achievement> findByUserId(int userId) throws SQLException {
        List<Achievement> list = new ArrayList<>();
        String sql = "SELECT * FROM achievements WHERE user_id = ? ORDER BY earned_at DESC";
        try (Connection c = DBHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Achievement a = new Achievement();
                    a.setId(rs.getInt("id"));
                    a.setUserId(rs.getInt("user_id"));
                    a.setBadgeKey(rs.getString("badge_key"));
                    a.setEarnedAt(rs.getString("earned_at"));
                    list.add(a);
                }
            }
        }
        return list;
    }

    public int totalCorrectAnswers(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(correct_count), 0) FROM game_history WHERE user_id = ?";
        try (Connection c = DBHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
