package youxi.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import youxi.util.DBHelper;

public class CheckInLogDAO {

    public boolean hasCheckedInToday(int userId) throws SQLException {
        String today = LocalDate.now().toString();
        String sql = "SELECT COUNT(*) FROM check_in_log WHERE user_id = ? AND check_in_date = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, today);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void insert(int userId, String date) throws SQLException {
        String sql = "INSERT INTO check_in_log (user_id, check_in_date) VALUES (?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, date);
            ps.executeUpdate();
        }
    }

    public int getStreak(int userId) throws SQLException {
        String sql = "SELECT check_in_date FROM check_in_log WHERE user_id = ? ORDER BY check_in_date DESC";
        List<String> dates = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) dates.add(rs.getString("check_in_date"));
            }
        }

        if (dates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        // 最近一次打卡必须是今天或昨天，否则连续中断
        LocalDate firstDate = LocalDate.parse(dates.get(0));
        if (firstDate.isBefore(today.minusDays(1))) return 0;

        int streak = 0;
        LocalDate expected = today;
        for (String d : dates) {
            LocalDate date = LocalDate.parse(d);
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (date.equals(expected.plusDays(1))) {
                // same day duplicate, skip
            } else {
                break;
            }
        }
        return streak;
    }
}
