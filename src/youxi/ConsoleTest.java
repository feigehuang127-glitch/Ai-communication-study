package youxi;

import youxi.dao.QuestionDAO;
import youxi.dao.UserDAO;
import youxi.model.Question;
import youxi.model.User;
import youxi.util.DBHelper;
import java.sql.Connection;
import java.util.List;

public class ConsoleTest {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Phase 1 Console Verify ===\n");

        // 1. Test DB connection
        try (Connection conn = DBHelper.getConnection()) {
            System.out.println("[OK] DB connection success");
        } catch (Exception e) {
            System.err.println("[FAIL] DB connection: " + e.getMessage());
            return;
        }

        // 2. Test user query
        try {
            UserDAO userDAO = new UserDAO();
            User player = userDAO.findByUsername("player");
            if (player != null) {
                System.out.printf("[OK] User: id=%d, username=%s, role=%s, rank=%s, score=%d%n",
                        player.getId(), player.getUsername(), player.getRole(),
                        player.getRank(), player.getTotalScore());
            } else {
                System.out.println("[FAIL] user 'player' not found");
            }
        } catch (Exception e) {
            System.err.println("[FAIL] UserDAO: " + e.getMessage());
        }

        // 3. Test question queries
        try {
            QuestionDAO questionDAO = new QuestionDAO();

            List<Question> all = questionDAO.findAll();
            System.out.printf("[OK] Total questions: %d%n", all.size());
            if (!all.isEmpty()) {
                Question first = all.get(0);
                String cat = first.getCategory();
                System.out.printf("[DEBUG] Q1: category='%s', hex=%s%n",
                    cat, bytesToHex(cat.getBytes("UTF-8")));
            }

            List<Question> txList = questionDAO.findByCategory("通信原理");
            List<Question> wlList = questionDAO.findByCategory("数据通信网");

            System.out.printf("[OK] 通信原理 count: %d%n", txList.size());
            for (Question q : txList) {
                System.out.printf("   [%s][diff=%d] %s%n", q.getType(), q.getDifficulty(), q.getContent());
            }

            System.out.printf("[OK] 数据通信网 count: %d%n", wlList.size());
            for (Question q : wlList) {
                System.out.printf("   [%s][diff=%d] %s%n", q.getType(), q.getDifficulty(), q.getContent());
            }

        } catch (Exception e) {
            System.err.println("[FAIL] QuestionDAO: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Verify Complete ===");
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }
}
