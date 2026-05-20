package youxi.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import youxi.model.WrongQuestion;
import youxi.util.DBHelper;

public class WrongQuestionDAO {

    public void insertOrIncrement(int userId, int questionId) throws SQLException {
        String findSql = "SELECT id, wrong_count FROM wrong_questions WHERE user_id = ? AND question_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String upd = "UPDATE wrong_questions SET wrong_count = wrong_count + 1, " +
                                 "correct_streak = 0, last_wrong_at = CURRENT_TIMESTAMP WHERE id = ?";
                    try (PreparedStatement updPs = conn.prepareStatement(upd)) {
                        updPs.setInt(1, rs.getInt("id"));
                        updPs.executeUpdate();
                    }
                } else {
                    String ins = "INSERT INTO wrong_questions (user_id, question_id) VALUES (?, ?)";
                    try (PreparedStatement insPs = conn.prepareStatement(ins)) {
                        insPs.setInt(1, userId);
                        insPs.setInt(2, questionId);
                        insPs.executeUpdate();
                    }
                }
            }
        }
    }

    public void incrementStreak(int userId, int questionId) throws SQLException {
        String sql = "UPDATE wrong_questions SET correct_streak = correct_streak + 1 " +
                     "WHERE user_id = ? AND question_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, questionId);
            ps.executeUpdate();
        }
    }

    /** 连续答对 2 次以上则删除错题记录（出狱） */
    public void removeIfReleased(int userId, int questionId) throws SQLException {
        String sql = "DELETE FROM wrong_questions WHERE user_id = ? AND question_id = ? AND correct_streak >= 2";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, questionId);
            ps.executeUpdate();
        }
    }

    public List<WrongQuestion> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM wrong_questions WHERE user_id = ? ORDER BY last_wrong_at DESC";
        List<WrongQuestion> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rowToWrongQuestion(rs));
            }
        }
        return list;
    }

    /** 手动删除某道错题（用户点击"从错题本移除"） */
    public void delete(int userId, int questionId) throws SQLException {
        String sql = "DELETE FROM wrong_questions WHERE user_id = ? AND question_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, questionId);
            ps.executeUpdate();
        }
    }

    private WrongQuestion rowToWrongQuestion(ResultSet rs) throws SQLException {
        WrongQuestion wq = new WrongQuestion();
        wq.setId(rs.getInt("id"));
        wq.setUserId(rs.getInt("user_id"));
        wq.setQuestionId(rs.getInt("question_id"));
        wq.setWrongCount(rs.getInt("wrong_count"));
        wq.setCorrectStreak(rs.getInt("correct_streak"));
        return wq;
    }
}
