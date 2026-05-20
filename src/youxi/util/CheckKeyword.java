package youxi.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class CheckKeyword {
    public static void main(String[] args) throws Exception {
        String kw = args.length > 0 ? args[0] : "低质量";
        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream("check_result.txt"), StandardCharsets.UTF_8));
             Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT id, category, CONCAT(LEFT(explanation, 120), '...') FROM questions WHERE explanation LIKE ?")) {
            ps.setString(1, "%" + kw + "%");
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    out.printf("[%d] ID=%d | %s%n  解析: %s%n%n",
                            count, rs.getInt(1), rs.getString(2), rs.getString(3));
                }
                out.printf("共找到 %d 条包含「%s」的记录。%n", count, kw);
                System.out.printf("找到 %d 条包含「%s」的记录，已写入 check_result.txt%n", count, kw);
            }
        }
        DBHelper.close();
    }
}
