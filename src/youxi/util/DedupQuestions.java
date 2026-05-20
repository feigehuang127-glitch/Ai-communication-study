package youxi.util;

import java.sql.*;
import java.util.*;

/**
 * 删除 questions 表中的重复题目（同 category + content 保留 id 最小的那条）。
 */
public class DedupQuestions {

    public static void main(String[] args) throws SQLException {
        List<Integer> toDelete = new ArrayList<>();

        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT category, content, COUNT(*) AS cnt, MIN(id) AS keep_id " +
                 "FROM questions GROUP BY category, content HAVING cnt > 1")) {

            while (rs.next()) {
                String cat = rs.getString("category");
                String content = rs.getString("content");
                int keepId = rs.getInt("keep_id");
                int dupCount = rs.getInt("cnt") - 1;

                // 找到该组中 id > keep_id 的记录
                try (PreparedStatement ps = conn.prepareStatement(
                         "SELECT id FROM questions WHERE category=? AND content=? AND id > ?")) {
                    ps.setString(1, cat);
                    ps.setString(2, content);
                    ps.setInt(3, keepId);
                    try (ResultSet rs2 = ps.executeQuery()) {
                        while (rs2.next()) {
                            toDelete.add(rs2.getInt("id"));
                        }
                    }
                }
                System.out.printf("[DUP] category=%s, keep=%d, delete=%d%n", cat, keepId, dupCount);
            }
        }

        if (toDelete.isEmpty()) {
            System.out.println("没有发现重复数据。");
            return;
        }

        System.out.printf("%n共发现 %d 条重复记录，开始删除...%n", toDelete.size());

        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM questions WHERE id = ?")) {
                for (int id : toDelete) {
                    ps.setInt(1, id);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        }

        System.out.printf("成功删除 %d 条重复记录。%n", toDelete.size());
        DBHelper.close();
    }
}
