package youxi.service;

import java.awt.Color;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import youxi.dao.AchievementDAO;
import youxi.dao.CheckInLogDAO;
import youxi.dao.UserDAO;
import youxi.model.Badge;
import youxi.model.User;
import youxi.util.BCryptUtil;
import youxi.util.Config;

public class UserService {

    private final UserDAO userDAO = new UserDAO();
    private final CheckInLogDAO checkInLogDAO = new CheckInLogDAO();

    // ── 段位体系 ──

    public static class RankTier {
        public final int index;
        public final int minScore;
        public final String name;
        public final String icon;
        public final Color color;

        public RankTier(int index, int minScore, String name, String icon, Color color) {
            this.index = index;
            this.minScore = minScore;
            this.name = name;
            this.icon = icon;
            this.color = color;
        }
    }

    private static final List<RankTier> RANK_TIERS = new ArrayList<>();

    static {
        String[][] defaults = {
            {"0", "0",  "青铜", "○", "#CD7F32"},
            {"1", "300","白银", "◎", "#C0C0C0"},
            {"2", "800","黄金", "●", "#FFA726"},
            {"3", "2000","铂金","◇", "#26C6DA"},
            {"4", "4000","钻石","◆", "#42A5F5"},
            {"5", "7000","大师","♛", "#AB47BC"},
            {"6", "12000","宗师","★","#EF5350"},
            {"7", "20000","王者","♛","#FFD700"},
        };
        for (String[] d : defaults) {
            int index = Integer.parseInt(d[0]);
            int minScore = Integer.parseInt(Config.get("rank." + index, d[1]));
            String name = Config.get("rank." + index + ".name", d[2]);
            String icon = d[3];
            Color color = Color.decode(d[4]);
            RANK_TIERS.add(new RankTier(index, minScore, name, icon, color));
        }
    }

    public static List<RankTier> getRankTiers() { return Collections.unmodifiableList(RANK_TIERS); }

    public static RankTier getRankTier(String rankName) {
        for (RankTier t : RANK_TIERS) {
            if (t.name.equals(rankName)) return t;
        }
        return RANK_TIERS.get(0);
    }

    public static int getRankIndex(String rankName) {
        for (RankTier t : RANK_TIERS) {
            if (t.name.equals(rankName)) return t.index;
        }
        return 0;
    }

    public static String scoreToRank(int score) {
        for (int i = RANK_TIERS.size() - 1; i >= 0; i--) {
            if (score >= RANK_TIERS.get(i).minScore) return RANK_TIERS.get(i).name;
        }
        return RANK_TIERS.get(0).name;
    }

    public static int[] getDifficultyRange(String rank) {
        int index = getRankIndex(rank);
        if (index <= 1) return new int[]{1, 2};
        if (index <= 3) return new int[]{2, 4};
        if (index <= 5) return new int[]{3, 5};
        return new int[]{4, 5};
    }

    // ── 用户操作 ──

    public User loginBySession(String username) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user == null) return null;
        String today = LocalDate.now().toString();
        if (!today.equals(user.getLastCheckinDate())) {
            userDAO.updateCheckinDate(user.getId(), today);
            user.setLastCheckinDate(today);
        }
        return user;
    }

    public User login(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user == null) return null;
        if (!BCryptUtil.check(password, user.getPasswordHash())) return null;

        String today = LocalDate.now().toString();
        if (!today.equals(user.getLastCheckinDate())) {
            userDAO.updateCheckinDate(user.getId(), today);
            user.setLastCheckinDate(today);
        }
        return user;
    }

    public User register(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty()) return null;
        if (password == null || password.length() < 6) return null;

        User existing = userDAO.findByUsername(username.trim());
        if (existing != null) return null;

        String hash = BCryptUtil.hash(password);
        return userDAO.insert(username.trim(), hash);
    }

    public CheckinResult checkin(User user) throws SQLException {
        boolean already = checkInLogDAO.hasCheckedInToday(user.getId());
        if (already) {
            int streak = checkInLogDAO.getStreak(user.getId());
            return new CheckinResult(false, streak, user.getTotalScore());
        }

        String today = LocalDate.now().toString();
        checkInLogDAO.insert(user.getId(), today);
        updateScoreAndRank(user, 1);
        userDAO.updateCheckinDate(user.getId(), today);
        user.setLastCheckinDate(today);

        int streak = checkInLogDAO.getStreak(user.getId());
        if (streak >= 7) {
            try { new AchievementDAO().award(user.getId(), Badge.DAILY_STREAK_7.key()); }
            catch (SQLException ignored) {}
        }
        return new CheckinResult(true, streak, user.getTotalScore());
    }

    public boolean hasCheckedInToday(int userId) throws SQLException {
        return checkInLogDAO.hasCheckedInToday(userId);
    }

    public int getStreak(int userId) throws SQLException {
        return checkInLogDAO.getStreak(userId);
    }

    public static class CheckinResult {
        public final boolean success;
        public final int streak;
        public final int totalScore;

        public CheckinResult(boolean success, int streak, int totalScore) {
            this.success = success;
            this.streak = streak;
            this.totalScore = totalScore;
        }
    }

    public void updateScoreAndRank(User user, int scoreDelta) throws SQLException {
        int newScore = user.getTotalScore() + scoreDelta;
        int rankIdx = getRankIndex(user.getRank());
        if (scoreDelta < 0 && rankIdx <= 1) {
            newScore = user.getTotalScore();
        }
        String newRank = scoreToRank(newScore);
        userDAO.updateScore(user.getId(), newScore);
        userDAO.updateRank(user.getId(), newRank);
        user.setTotalScore(newScore);
        user.setRank(newRank);
    }
}
