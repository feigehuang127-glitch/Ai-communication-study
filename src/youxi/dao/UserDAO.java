package youxi.dao;

import youxi.model.User;
import youxi.util.DBHelper;
import java.sql.*;

public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowToUser(rs);
                }
            }
        }
        return null;
    }

    public User insert(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, total_score, `rank`) VALUES (?, ?, 'player', 0, '青铜')";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    User u = new User();
                    u.setId(keys.getInt(1));
                    u.setUsername(username);
                    u.setPasswordHash(passwordHash);
                    u.setRole("player");
                    u.setTotalScore(0);
                    u.setRank("青铜");
                    return u;
                }
            }
        }
        return null;
    }

    public void updateScore(int userId, int newScore) throws SQLException {
        String sql = "UPDATE users SET total_score = ? WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newScore);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void updateRank(int userId, String rank) throws SQLException {
        String sql = "UPDATE users SET `rank` = ? WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rank);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void updateCheckinDate(int userId, String date) throws SQLException {
        String sql = "UPDATE users SET last_checkin_date = ? WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void updatePassword(int userId, String newHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public static class LeaderboardEntry {
        public final String username;
        public final int totalScore;
        public final String rank;

        public LeaderboardEntry(String username, int totalScore, String rank) {
            this.username = username;
            this.totalScore = totalScore;
            this.rank = rank;
        }
    }

    public java.util.List<LeaderboardEntry> getTopPlayers(int limit) throws SQLException {
        java.util.List<LeaderboardEntry> list = new java.util.ArrayList<>();
        String sql = "SELECT username, total_score, `rank` FROM users WHERE role = 'player' ORDER BY total_score DESC LIMIT ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LeaderboardEntry(
                        rs.getString("username"),
                        rs.getInt("total_score"),
                        rs.getString("rank")));
                }
            }
        }
        return list;
    }

    private User rowToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setTotalScore(rs.getInt("total_score"));
        u.setRank(rs.getString("rank"));
        String date = rs.getString("last_checkin_date");
        u.setLastCheckinDate(date);
        return u;
    }
}
