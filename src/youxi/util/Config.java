package youxi.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** 读取 config.properties 配置 */
public class Config {

    private static final Properties props = new Properties();

    static {
        File f = new File("config.properties");
        if (f.exists()) {
            try (Reader r = new InputStreamReader(
                    new FileInputStream(f), StandardCharsets.UTF_8)) {
                props.load(r);
            } catch (IOException e) {
                System.err.println("[Config] 加载配置失败: " + e.getMessage());
            }
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String v = props.getProperty(key);
        if (v == null || v.isEmpty()) return defaultValue;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return defaultValue; }
    }

    public static void set(String key, String value) {
        props.setProperty(key, value);
        File f = new File("config.properties");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            props.store(w, null);
        } catch (IOException e) {
            System.err.println("[Config] 保存配置失败: " + e.getMessage());
        }
    }

    // ── 便捷方法 ──

    public static String dbUrl()      { return get("db.url"); }
    public static String dbUser()     { return get("db.username"); }
    public static String dbPassword() { return get("db.password"); }
    public static int    dbMaxPool()  { return getInt("db.maxPoolSize", 10); }
    public static int    dbMinIdle()  { return getInt("db.minIdle", 2); }
    public static int    dbConnTimeout() { return getInt("db.connectionTimeout", 3000); }

    public static int questionsPerRound()  { return getInt("game.questions_per_round", 10); }
    public static int timePerQuestion()    { return getInt("game.time_per_question", 10); }
    public static int comboThreshold()     { return getInt("game.combo_threshold", 5); }
    public static int baseScore()          { return getInt("game.base_score_per_question", 10); }
    public static int dailyScoreMultiplier() { return getInt("game.daily_score_multiplier", 2); }
    public static int difficultyPreference() {
        String v = get("game.difficulty_preference", "默认");
        if ("偏易".equals(v)) return -1;
        if ("偏难".equals(v)) return 1;
        return 0;
    }

    public static String uiFont() { return get("ui.font", "微软雅黑"); }
    public static boolean uiFullscreen() { return "true".equalsIgnoreCase(get("ui.fullscreen", "true")); }
}
