package youxi.service;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import youxi.dao.QuestionDAO;
import youxi.model.Question;

/**
 * 题目缓存单例 — 启动时预加载全部题目到内存。
 * 按学科和 ID 建立索引，避免每次答题都查询数据库。
 */
public class QuestionCache {

    private static volatile QuestionCache instance;

    private final List<Question> all;
    private final Map<Integer, Question> byId;
    private final Map<String, List<Question>> byCategory;

    private QuestionCache() {
        QuestionDAO dao = new QuestionDAO();
        List<Question> loaded;
        try {
            loaded = dao.findAll();
        } catch (SQLException e) {
            System.err.println("[QuestionCache] 加载题目失败: " + e.getMessage());
            loaded = Collections.emptyList();
        }
        all = Collections.unmodifiableList(loaded);
        byId = new HashMap<>(loaded.size());
        byCategory = new HashMap<>();

        for (Question q : loaded) {
            byId.put(q.getId(), q);
            byCategory.computeIfAbsent(q.getCategory(), k -> new ArrayList<>()).add(q);
        }

        System.out.println("[QuestionCache] 已缓存 " + loaded.size() + " 题，"
                + byCategory.size() + " 个学科");
    }

    public static QuestionCache getInstance() {
        if (instance == null) {
            synchronized (QuestionCache.class) {
                if (instance == null) {
                    instance = new QuestionCache();
                }
            }
        }
        return instance;
    }

    /** 重新加载（导入新题后调用） */
    public static void refresh() {
        synchronized (QuestionCache.class) {
            instance = null;
        }
    }

    // ── 查询方法 ──

    public Question getById(int id) {
        return byId.get(id);
    }

    public List<Question> getByCategory(String category) {
        List<Question> list = byCategory.get(category);
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    public List<Question> getByCategoryAndDifficulty(String category, int minD, int maxD) {
        List<Question> list = byCategory.get(category);
        if (list == null) return Collections.emptyList();
        List<Question> result = new ArrayList<>();
        for (Question q : list) {
            int d = q.getDifficulty();
            if (d >= minD && d <= maxD) result.add(q);
        }
        return result;
    }

    /** 从缓存中随机选取指定数量的题目 */
    public List<Question> pickRandom(String category, int minD, int maxD, int count) {
        List<Question> pool = new ArrayList<>(getByCategoryAndDifficulty(category, minD, maxD));

        // 不够时扩大难度范围
        if (pool.size() < count) {
            Set<Question> merged = new HashSet<>(pool);
            merged.addAll(getByCategoryAndDifficulty(category,
                    Math.max(1, minD - 2), Math.min(5, maxD + 2)));
            pool = new ArrayList<>(merged);
        }
        // 还是不够就用该学科全部题目
        if (pool.size() < count) {
            List<Question> allCat = byCategory.get(category);
            if (allCat != null) {
                Set<Question> merged = new HashSet<>(pool);
                merged.addAll(allCat);
                pool = new ArrayList<>(merged);
            }
        }

        Collections.shuffle(pool);
        return pool.subList(0, Math.min(count, pool.size()));
    }

    public List<Question> pickRandomSeeded(String category, int minD, int maxD, int count, long seed) {
        List<Question> pool = new ArrayList<>(getByCategoryAndDifficulty(category, minD, maxD));
        if (pool.size() < count) {
            Set<Question> merged = new HashSet<>(pool);
            merged.addAll(getByCategoryAndDifficulty(category,
                    Math.max(1, minD - 2), Math.min(5, maxD + 2)));
            pool = new ArrayList<>(merged);
        }
        if (pool.size() < count) {
            List<Question> allCat = byCategory.get(category);
            if (allCat != null) {
                Set<Question> merged = new HashSet<>(pool);
                merged.addAll(allCat);
                pool = new ArrayList<>(merged);
            }
        }
        pool.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        Collections.shuffle(pool, new java.util.Random(seed));
        return pool.subList(0, Math.min(count, pool.size()));
    }

    public int size() { return all.size(); }
    public int categoryCount() { return byCategory.size(); }
    public Set<String> getCategories() { return byCategory.keySet(); }
}
