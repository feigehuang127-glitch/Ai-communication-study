package youxi.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class FindHuikao {

    public static void main(String[] args) throws Exception {
        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream("huikao_ids.txt"), StandardCharsets.UTF_8));
             Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id, category, type, " +
                 "CONCAT(LEFT(content, 40), CASE WHEN CHAR_LENGTH(content)>40 THEN '...' ELSE '' END) AS preview, " +
                 "explanation " +
                 "FROM questions WHERE explanation LIKE '%慧考智学%' " +
                 "ORDER BY id")) {

            int count = 0;
            while (rs.next()) {
                count++;
                int id = rs.getInt("id");
                String cat = rs.getString("category");
                String type = rs.getString("type");
                String preview = rs.getString("preview");
                out.printf("[%d] ID=%d | %s | %s%n  题目: %s%n%n", count, id, cat, type, preview);
            }

            out.printf("%n共找到 %d 条。%n", count);
            System.out.println("结果已写入 huikao_ids.txt，共 " + count + " 条。");
        }
        DBHelper.close();
    }
}
