package youxi.util;

import java.sql.*;

public class CheckExplanation {

    public static void main(String[] args) throws SQLException {
        int total = 0, withExp = 0, withoutExp = 0;

        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement()) {

            // 总数
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM questions")) {
                if (rs.next()) total = rs.getInt(1);
            }

            // 有解析的
            try (ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) FROM questions WHERE explanation IS NOT NULL AND explanation <> ''")) {
                if (rs.next()) withExp = rs.getInt(1);
            }

            withoutExp = total - withExp;

            System.out.println("═══════════════════════════");
            System.out.println("题目总数: " + total);
            System.out.println("已有解析: " + withExp);
            System.out.println("缺解析:   " + withoutExp);
            System.out.println("覆盖率:   " + (total > 0 ? String.format("%.1f%%", 100.0 * withExp / total) : "N/A"));
            System.out.println("═══════════════════════════");

            if (withoutExp > 0) {
                System.out.println("\n缺少解析的题目按学科分布：");
                try (ResultSet rs = st.executeQuery(
                        "SELECT COALESCE(category,'未分类') AS cat, COUNT(*) AS cnt " +
                        "FROM questions WHERE explanation IS NULL OR explanation = '' " +
                        "GROUP BY category ORDER BY cnt DESC")) {
                    while (rs.next()) {
                        System.out.printf("  %-20s %d 题%n", rs.getString("cat"), rs.getInt("cnt"));
                    }
                }
            }
        }
        DBHelper.close();
    }
}
