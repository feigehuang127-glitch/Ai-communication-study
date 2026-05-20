package youxi.dao;

import youxi.model.Question;
import youxi.util.DBHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    /** 查询所有题目（不分学科） */
    public List<Question> findAll() throws SQLException {
        String sql = "SELECT * FROM questions";
        List<Question> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rowToQuestion(rs));
            }
        }
        return list;
    }

    /** 按学科和难度范围查询题目 */
    public List<Question> findByCategoryAndDifficulty(String category, int minDiff, int maxDiff) throws SQLException {
        String sql = "SELECT * FROM questions WHERE category = ? AND difficulty BETWEEN ? AND ? ORDER BY RAND()";
        List<Question> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ps.setInt(2, minDiff);
            ps.setInt(3, maxDiff);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToQuestion(rs));
                }
            }
        }
        return list;
    }

    /** 按 ID 查询单个题目 */
    public Question findById(int id) throws SQLException {
        String sql = "SELECT * FROM questions WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rowToQuestion(rs);
            }
        }
        return null;
    }

    /** 查询所有没有解析的题目 */
    public List<Question> findWithoutExplanation() throws SQLException {
        String sql = "SELECT * FROM questions WHERE explanation IS NULL OR explanation = ''";
        List<Question> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(rowToQuestion(rs));
        }
        return list;
    }

    /** 更新题目的解析字段 */
    public void updateExplanation(int questionId, String explanation) throws SQLException {
        String sql = "UPDATE questions SET explanation = ? WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, explanation);
            ps.setInt(2, questionId);
            ps.executeUpdate();
        }
    }

    /** 按学科查询题目（不限难度） */
    public List<Question> findByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM questions WHERE category = ?";
        List<Question> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToQuestion(rs));
                }
            }
        }
        return list;
    }

    private Question rowToQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setCategory(rs.getString("category"));
        q.setType(rs.getString("type"));
        q.setContent(rs.getString("content"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setAnswer(rs.getString("answer"));
        q.setExplanation(rs.getString("explanation"));
        q.setDifficulty(rs.getInt("difficulty"));
        return q;
    }
}
