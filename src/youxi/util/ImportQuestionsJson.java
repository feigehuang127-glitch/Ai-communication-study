package youxi.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 从 data/*.json 批量导入题目到数据库。
 * JSON 格式：
 * [{"id":1, "type":"single_choice", "question":"...",
 *   "options":[{"label":"A","text":"..."}, ...],
 *   "answer":"D", "analysis":"..."}]
 *
 * 用法：java -cp "lib/*;out" youxi.util.ImportQuestionsJson
 */
public class ImportQuestionsJson {

    public static void main(String[] args) {
        String[] files = {
            "data/通信原理.json",
            "data/通信原理1.json",
            "data/通信原理3.json",
            "data/通信原理4.json",
            "data/数据通信网.json",
            "data/数据通信网2.json",
            "data/光纤传输1.json",
            "data/光纤传输2.json",
            "data/宽带接入技术1.json",
            "data/宽带接入技术2.json",
            "data/现代交换技术1.json",
            "data/现代交换技术2.json",
            "data/现代交换技术3.json",
            "data/现代交换技术4.json",
            "data/信息通信新技术.json",
        };

        int totalCount = 0;
        int totalSkip = 0;

        for (String path : files) {
            File f = new File(path);
            if (!f.exists()) {
                System.err.println("[WARN] 文件不存在: " + f.getAbsolutePath());
                continue;
            }

            try {
                String jsonStr = readFile(f);
                List<QuestionItem> items = parseJsonArray(jsonStr);
                int[] result = insertQuestions(items);
                System.out.printf("[%s] 导入 %d 题，跳过 %d 题%n", f.getName(), result[0], result[1]);
                totalCount += result[0];
                totalSkip += result[1];
            } catch (Exception e) {
                System.err.println("[FAIL] " + path + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.printf("%n[DONE] 总计导入 %d 题，跳过 %d 题%n", totalCount, totalSkip);
        DBHelper.close();
    }

    private static String readFile(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    /** 简易 JSON 数组解析（不依赖第三方库） */
    private static List<QuestionItem> parseJsonArray(String json) {
        List<QuestionItem> items = new ArrayList<>();
        int depth = 0, objStart = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) objStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    items.add(parseOneObject(json.substring(objStart, i + 1)));
                    objStart = -1;
                }
            }
        }
        return items;
    }

    private static QuestionItem parseOneObject(String obj) {
        QuestionItem item = new QuestionItem();
        item.type = extractString(obj, "type");
        item.category = extractString(obj, "category");
        item.question = extractString(obj, "question");
        item.answer = extractString(obj, "answer");
        item.analysis = extractString(obj, "analysis");

        // 解析 options 数组
        int optIdx = obj.indexOf("\"options\"");
        if (optIdx >= 0) {
            int arrStart = obj.indexOf("[", optIdx);
            int arrEnd = findMatchingBracket(obj, arrStart);
            if (arrStart >= 0 && arrEnd > arrStart) {
                String arrStr = obj.substring(arrStart + 1, arrEnd);
                item.options = parseOptionsArray(arrStr);
            }
        }
        return item;
    }

    private static List<OptionItem> parseOptionsArray(String arr) {
        List<OptionItem> opts = new ArrayList<>();
        int depth = 0, objStart = -1;
        for (int i = 0; i < arr.length(); i++) {
            char c = arr.charAt(i);
            if (c == '{') {
                if (depth == 0) objStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    String obj = arr.substring(objStart, i + 1);
                    OptionItem opt = new OptionItem();
                    opt.label = extractString(obj, "label");
                    opt.text = extractString(obj, "text");
                    opts.add(opt);
                    objStart = -1;
                }
            }
        }
        return opts;
    }

    private static int findMatchingBracket(String s, int start) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    /** 提取 "key": "value" 或 "key": "" 中的 value */
    private static String extractString(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return "";

        int colonIdx = json.indexOf(":", keyIdx);
        if (colonIdx < 0) return "";

        // 跳过冒号和空白
        int valStart = colonIdx + 1;
        while (valStart < json.length() && (json.charAt(valStart) == ' ' || json.charAt(valStart) == '\t' || json.charAt(valStart) == '\n')) {
            valStart++;
        }

        if (valStart >= json.length()) return "";

        if (json.charAt(valStart) == '"') {
            int valEnd = valStart + 1;
            while (valEnd < json.length()) {
                char c = json.charAt(valEnd);
                if (c == '"' && json.charAt(valEnd - 1) != '\\') break;
                valEnd++;
            }
            return unescape(json.substring(valStart + 1, valEnd));
        }
        return "";
    }

    private static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\n", "\n").replace("\\t", "\t");
    }

    private static int[] insertQuestions(List<QuestionItem> items) throws SQLException {
        String sql = "INSERT INTO questions (category, type, content, option_a, option_b, option_c, option_d, answer, explanation, difficulty) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int count = 0, skip = 0;

        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (QuestionItem item : items) {
                    if (item.question.isEmpty()) { skip++; continue; }

                    String type = mapType(item.type);
                    String optA = "", optB = "", optC = "", optD = "";
                    if (item.options != null) {
                        if (item.options.size() > 0) optA = item.options.get(0).text;
                        if (item.options.size() > 1) optB = item.options.get(1).text;
                        if (item.options.size() > 2) optC = item.options.get(2).text;
                        if (item.options.size() > 3) optD = item.options.get(3).text;
                    }

                    String cat = item.category != null && !item.category.isEmpty()
                            ? item.category : "未分类";
                    ps.setString(1, cat);
                    ps.setString(2, type);
                    ps.setString(3, item.question);
                    ps.setString(4, optA);
                    ps.setString(5, optB);
                    ps.setString(6, optC.isEmpty() ? null : optC);
                    ps.setString(7, optD.isEmpty() ? null : optD);
                    ps.setString(8, item.answer);
                    ps.setString(9, item.analysis.isEmpty() ? null : item.analysis);
                    ps.setInt(10, 3); // 默认难度
                    ps.addBatch();
                    count++;
                }
                ps.executeBatch();
            }
            conn.commit();
        }
        return new int[]{count, skip};
    }

    /** 英文题型 → 中文题型 */
    private static String mapType(String t) {
        switch (t) {
            case "single_choice": return "单选";
            case "multi_choice":  return "多选";
            case "true_false":    return "判断";
            case "judge":         return "判断";
            default:              return "单选";
        }
    }

    // ── 内部类 ──

    static class QuestionItem {
        String type, category, question, answer, analysis;
        List<OptionItem> options;
    }

    static class OptionItem {
        String label, text;
    }
}
