package youxi.util;

import java.sql.*;

/**
 * 清空包含「慧考智学」的解析，准备重新生成。
 */
public class ClearHuikao {

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE questions SET explanation = '' WHERE explanation LIKE '%慧考智学%'")) {
            int n = ps.executeUpdate();
            System.out.println("已清空 " + n + " 题的解析。");
        }
        DBHelper.close();
    }
}
