package youxi.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportQuestions {

    public static void main(String[] args) {
        String csvPath = "data/questions.csv";
        File csvFile = new File(csvPath);
        if (!csvFile.exists()) {
            System.err.println("[ERROR] 找不到文件: " + csvFile.getAbsolutePath());
            return;
        }

        int count = 0, skip = 0;

        try (Connection conn = DBHelper.getConnection();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            if (header == null || !header.startsWith("category")) {
                System.err.println("[ERROR] CSV 缺少表头");
                return;
            }

            String sql = "INSERT INTO questions (category, type, content, option_a, option_b, option_c, option_d, answer, explanation, difficulty) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = parseCSVLine(line);
                    if (parts.length < 7) {
                        System.err.println("[SKIP] 字段不足: " + line.substring(0, Math.min(60, line.length())));
                        skip++;
                        continue;
                    }

                    String category = parts[0].trim();
                    String type = parts[1].trim();
                    String content = parts[2].trim();
                    String answer = parts[4].trim();
                    String explanation = parts[5].trim();
                    int difficulty = Integer.parseInt(parts[6].trim());

                    // 解析选项: "A.xxx$B.yyy$C.zzz" -> 拆分成 4 个字段，去掉 A. 前缀
                    String[] opts = parseOptions(parts[3].trim());
                    String optA = opts.length > 0 ? stripPrefix(opts[0]) : null;
                    String optB = opts.length > 1 ? stripPrefix(opts[1]) : null;
                    String optC = opts.length > 2 ? stripPrefix(opts[2]) : null;
                    String optD = opts.length > 3 ? stripPrefix(opts[3]) : null;

                    ps.setString(1, category);
                    ps.setString(2, type);
                    ps.setString(3, content);
                    ps.setString(4, optA != null ? optA : "");
                    ps.setString(5, optB != null ? optB : "");
                    ps.setString(6, optC);
                    ps.setString(7, optD);
                    ps.setString(8, answer);
                    ps.setString(9, explanation);
                    ps.setInt(10, difficulty);
                    ps.addBatch();
                    count++;
                }
                ps.executeBatch();
            }
            conn.commit();
            System.out.printf("[DONE] 导入 %d 题，跳过 %d 行%n", count, skip);

        } catch (Exception e) {
            System.err.println("[FAIL] " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBHelper.close();
        }
    }

    /** 简易 CSV 行解析：处理引号包裹的字段（字段内含逗号） */
    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }

    /** 按 $ 拆分选项 */
    private static String[] parseOptions(String optionsField) {
        return optionsField.split("\\$");
    }

    /** 去掉选项前面的 A. / B. / C. / D. 前缀 */
    private static String stripPrefix(String opt) {
        opt = opt.trim();
        if (opt.length() >= 2 && opt.charAt(1) == '.') {
            return opt.substring(2).trim();
        }
        if (opt.length() >= 2 && opt.charAt(1) == ' ') {
            return opt.substring(2).trim();
        }
        return opt;
    }
}
