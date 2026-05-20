package youxi.util;

import java.net.URI;
import java.net.http.*;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import youxi.dao.QuestionDAO;
import youxi.model.Question;

/**
 * 批量生成题目解析工具（支持大批量）。
 * 使用硅基流动 (SiliconFlow) 免费 API，模型 Qwen2.5-7B-Instruct。
 *
 * 用法：
 *   java -cp "lib/*;out" youxi.util.ExplainGenerator [BATCH_SIZE] [API_KEY]
 *
 * 默认每批 5 道题。自动跳过已有解析的题目（支持断点续传）。
 */
public class ExplainGenerator {

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL = "deepseek-v4-pro";
    private static final int MAX_RETRIES = 3;
    private static final int BATCH_DELAY_MS = 600;
    private static final int DEFAULT_BATCH_SIZE = 5;

    private final String apiKey;
    private final int batchSize;
    private final QuestionDAO questionDAO;
    private final HttpClient httpClient;

    public ExplainGenerator(String apiKey, int batchSize) {
        this.apiKey = apiKey;
        this.batchSize = Math.max(1, Math.min(10, batchSize));
        this.questionDAO = new QuestionDAO();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public static void main(String[] args) {
        String apiKey = null;
        int batchSize = DEFAULT_BATCH_SIZE;

        // 解析参数
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            if (arg.matches("\\d+")) {
                batchSize = Integer.parseInt(arg);
            } else if (arg.startsWith("sk-")) {
                apiKey = arg;
            }
        }
        // 环境变量兜底
        if (apiKey == null) {
            apiKey = System.getenv("SILICONFLOW_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("请设置 API Key：");
            System.out.println("  java -cp \"lib/*;out\" youxi.util.ExplainGenerator [每批题数] <API_KEY>");
            System.out.println("  或设置环境变量 SILICONFLOW_API_KEY");
            System.out.println();
            System.out.println("获取免费 API Key: https://cloud.siliconflow.cn/account/ak");
            System.exit(1);
        }

        ExplainGenerator generator = new ExplainGenerator(apiKey, batchSize);
        generator.run();
    }

    public void run() {
        System.out.println("╔═══════════════════════════════╗");
        System.out.println("║   题目解析批量生成器 v2       ║");
        System.out.println("╠═══════════════════════════════╣");
        System.out.println("║ API: DeepSeek                ║");
        System.out.println("║ 模型: " + padRight(MODEL, 22) + "║");
        System.out.println("║ 每批: " + batchSize + " 题                    ║");
        System.out.println("╚═══════════════════════════════╝");
        System.out.println();

        // 加载题目（自动跳过已有解析的）
        List<Question> questions;
        try {
            questions = questionDAO.findWithoutExplanation();
        } catch (SQLException e) {
            System.err.println("查询题目失败: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (questions.isEmpty()) {
            System.out.println("所有题目都已有解析，无需生成。");
            return;
        }

        System.out.println("待处理: " + questions.size() + " 道题");
        int totalBatches = (int) Math.ceil((double) questions.size() / batchSize);
        System.out.println("批次总数: " + totalBatches + " (每批 " + batchSize + " 题)");
        long estSeconds = totalBatches * (BATCH_DELAY_MS + 5000) / 1000;
        System.out.printf("预计耗时: %d 分 %d 秒%n", estSeconds / 60, estSeconds % 60);
        System.out.println();

        int success = 0;
        int fail = 0;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < questions.size(); i += batchSize) {
            int batchEnd = Math.min(i + batchSize, questions.size());
            List<Question> batch = questions.subList(i, batchEnd);
            int batchNum = i / batchSize + 1;

            System.out.printf("[%3d/%3d] %d道题 ", batchNum, totalBatches, batch.size());
            System.out.flush();

            // 尝试批量生成
            List<String> explanations = generateBatch(batch, batchNum, totalBatches);

            if (explanations != null && explanations.size() == batch.size()) {
                // 批量成功，写库
                int batchOk = 0;
                for (int j = 0; j < batch.size(); j++) {
                    Question q = batch.get(j);
                    String exp = explanations.get(j);
                    if (exp != null && !exp.isBlank()) {
                        try {
                            questionDAO.updateExplanation(q.getId(), exp.trim());
                            batchOk++;
                        } catch (SQLException e) {
                            System.err.printf("  写库失败 ID=%d: %s%n", q.getId(), e.getMessage());
                        }
                    }
                }
                success += batchOk;
                fail += (batch.size() - batchOk);
                System.out.printf("✓ %d/%d%n", batchOk, batch.size());
            } else {
                // 批量失败，逐题重试
                System.out.println("批量失败，逐题重试...");
                for (int j = 0; j < batch.size(); j++) {
                    Question q = batch.get(j);
                    System.out.printf("  题 %d ID=%d... ", j + 1, q.getId());
                    String exp = callSingleWithRetry(q);
                    if (exp != null && !exp.isBlank()) {
                        try {
                            questionDAO.updateExplanation(q.getId(), exp.trim());
                            success++;
                            System.out.println("✓");
                        } catch (SQLException e) {
                            fail++;
                            System.out.println("✗");
                        }
                    } else {
                        fail++;
                        System.out.println("✗");
                    }
                    if (j < batch.size() - 1) sleep(400);
                }
            }

            // 进度报告
            long elapsed = System.currentTimeMillis() - startTime;
            int done = success + fail;
            if (done > 0) {
                long eta = elapsed * (questions.size() - i - batch.size()) / Math.max(1, done);
                System.out.printf("  进度: %d/%d  成功: %d  失败: %d  ETA: %s%n%n",
                        i + batch.size(), questions.size(), success, fail,
                        formatDuration(eta));
            }

            if (i + batchSize < questions.size()) {
                sleep(BATCH_DELAY_MS);
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("╔═══════════════════════════════╗");
        System.out.printf("║  完成！成功 %d / 失败 %d     ║%n", success, fail);
        System.out.printf("║  耗时: %s            ║%n", padRight(formatDuration(totalTime), 20));
        System.out.println("╚═══════════════════════════════╝");
    }

    /** 批量生成一批题目 */
    private List<String> generateBatch(List<Question> batch, int batchNum, int totalBatches) {
        String prompt = buildBatchPrompt(batch);
        String systemPrompt = "你是一位通信知识专业老师，精通通信原理、数据通信网、光纤传输、宽带接入技术、现代交换技术和信息通信新技术。请为每道题生成简洁、准确的解析。严格按照指定格式输出。";

        String requestBody = "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":" + jsonString(systemPrompt) + "},"
                + "{\"role\":\"user\",\"content\":" + jsonString(prompt) + "}"
                + "],"
                + "\"temperature\":0.5,"
                + "\"max_tokens\":" + (batch.size() * 300) + ","
                + "\"stream\":false"
                + "}";

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(60))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    List<String> results = parseBatchResponse(response.body(), batch.size());
                    if (results != null) return results;
                } else {
                    String preview = response.body() != null
                            ? response.body().substring(0, Math.min(200, response.body().length())) : "";
                    System.out.printf("(HTTP %d: %s) ", response.statusCode(), preview);
                }
            } catch (Exception e) {
                System.out.printf("(异常: %s) ", e.getMessage());
            }

            if (attempt < MAX_RETRIES) {
                System.out.printf("重试%d... ", attempt);
                sleep(1500);
            }
        }
        return null;
    }

    /** 构造批量 prompt */
    private String buildBatchPrompt(List<Question> batch) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为以下").append(batch.size()).append("道题分别写解析。每道题的解析以【题N】开头，2-4句话，解释为什么正确答案是对的。\n\n");

        for (int i = 0; i < batch.size(); i++) {
            Question q = batch.get(i);
            sb.append("【题").append(i + 1).append("】\n");
            sb.append("学科：").append(nullToEmpty(q.getCategory())).append("\n");
            sb.append("题型：").append(nullToEmpty(q.getType())).append("\n");
            sb.append("题目：").append(nullToEmpty(q.getContent())).append("\n");
            sb.append("选项：").append(nullToEmpty(q.getOptions())).append("\n");
            sb.append("正确答案：").append(nullToEmpty(q.getAnswer())).append("\n");
            sb.append("\n");
        }

        sb.append("请依次输出每道题的解析，格式如下（不要加额外说明）：\n");
        for (int i = 0; i < batch.size(); i++) {
            sb.append("【题").append(i + 1).append("】<解析内容>\n");
        }
        return sb.toString();
    }

    /** 解析批量响应，提取每道题的解析 */
    private static List<String> parseBatchResponse(String body, int expectedCount) {
        if (body == null || body.isEmpty()) return null;

        // 提取 content
        String content = extractContent(body);
        if (content == null || content.isEmpty()) return null;

        List<String> results = new ArrayList<>();
        for (int i = 1; i <= expectedCount; i++) {
            String marker = "【题" + i + "】";
            int start = content.indexOf(marker);
            if (start < 0) {
                // 尝试其他格式
                marker = "[题" + i + "]";
                start = content.indexOf(marker);
                if (start < 0) {
                    marker = "## " + i;
                    start = content.indexOf(marker);
                    if (start < 0) {
                        marker = i + ".";
                        start = content.indexOf(marker);
                    }
                }
            }

            if (start < 0) {
                results.add(null);
                continue;
            }

            int contentStart = start + marker.length();
            int end;
            if (i < expectedCount) {
                // 找到下一个题号标记
                String nextMarker = "【题" + (i + 1) + "】";
                end = content.indexOf(nextMarker, contentStart);
                if (end < 0) {
                    nextMarker = "[题" + (i + 1) + "]";
                    end = content.indexOf(nextMarker, contentStart);
                }
                if (end < 0) {
                    nextMarker = "## " + (i + 1);
                    end = content.indexOf(nextMarker, contentStart);
                }
                if (end < 0) {
                    end = content.length();
                }
            } else {
                end = content.length();
            }

            String exp = content.substring(contentStart, end).trim();
            // 清理常见的前后缀
            exp = exp.replaceAll("^[：:\\s]+", "");
            exp = exp.replaceAll("\\s+$", "");
            results.add(exp.isEmpty() ? null : exp);
        }

        // 至少要有半数成功
        long nonNull = results.stream().filter(s -> s != null && !s.isBlank()).count();
        return nonNull >= expectedCount / 2 + 1 ? results : null;
    }

    /** 逐题生成（批量失败时的回退方案） */
    private String callSingleWithRetry(Question q) {
        String systemPrompt = "你是一位通信知识专业老师，精通通信原理、数据通信网、光纤传输、宽带接入技术、现代交换技术和信息通信新技术。请为题目生成简洁、准确的解析。";
        String userPrompt = "学科：" + nullToEmpty(q.getCategory()) + "\n"
                + "题型：" + nullToEmpty(q.getType()) + "\n"
                + "题目：" + nullToEmpty(q.getContent()) + "\n"
                + "选项：" + nullToEmpty(q.getOptions()) + "\n"
                + "正确答案：" + nullToEmpty(q.getAnswer()) + "\n\n"
                + "请写一段简洁解析（2-4句话），解释为什么正确答案是对的。直接输出解析，不加前缀。";

        String requestBody = "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":" + jsonString(systemPrompt) + "},"
                + "{\"role\":\"user\",\"content\":" + jsonString(userPrompt) + "}"
                + "],"
                + "\"temperature\":0.7,"
                + "\"max_tokens\":300,"
                + "\"stream\":false"
                + "}";

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String result = extractContent(response.body());
                    if (result != null && !result.isBlank()) {
                        result = result.replaceAll("^(解析[：:]?\\s*|【解析】\\s*)", "");
                        return result.trim();
                    }
                }
            } catch (Exception ignored) {}

            if (attempt < MAX_RETRIES) sleep(1000);
        }
        return null;
    }

    /** 从 OpenAI 兼容响应中提取 content */
    private static String extractContent(String body) {
        if (body == null || body.isEmpty()) return null;
        String key = "\"content\":\"";
        int start = body.indexOf(key);
        if (start < 0) {
            key = "\"content\": \"";
            start = body.indexOf(key);
            if (start < 0) return null;
        }

        int pos = start + key.length();
        StringBuilder sb = new StringBuilder();
        while (pos < body.length()) {
            char c = body.charAt(pos);
            if (c == '\\' && pos + 1 < body.length()) {
                char next = body.charAt(pos + 1);
                switch (next) {
                    case 'n':  sb.append('\n'); pos += 2; break;
                    case 't':  sb.append('\t'); pos += 2; break;
                    case 'r':  sb.append('\r'); pos += 2; break;
                    case '"':  sb.append('"');  pos += 2; break;
                    case '\\': sb.append('\\'); pos += 2; break;
                    case 'u':
                        if (pos + 6 <= body.length()) {
                            try {
                                sb.append((char) Integer.parseInt(
                                        body.substring(pos + 2, pos + 6), 16));
                                pos += 6;
                            } catch (NumberFormatException e) {
                                sb.append(c); pos++;
                            }
                        } else { sb.append(c); pos++; }
                        break;
                    default: sb.append(c); pos++; break;
                }
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
                pos++;
            }
        }
        return sb.toString().trim();
    }

    /** JSON 字符串转义 */
    private static String jsonString(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    private static String formatDuration(long ms) {
        long sec = ms / 1000;
        return String.format("%d分%d秒", sec / 60, sec % 60);
    }

    private static String padRight(String s, int n) {
        if (s == null) s = "";
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < n) sb.append(' ');
        return sb.toString();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
