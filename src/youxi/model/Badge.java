package youxi.model;

public enum Badge {
    FIRST_WIN("first_win", "初战告捷", "第一次赢得比赛", "★"),
    TEN_WINS("ten_wins", "身经百战", "累计赢得10场比赛", "⚔"),
    HUNDRED_CORRECT("hundred_correct", "百答不殆", "累计答对100题", "∞"),
    COMBO_MASTER("combo_master", "连击大师", "达成5连击胜利", "◆"),
    PERFECT_SCORE("perfect_score", "完美无缺", "单局全部答对", "◇"),
    SPEED_DEMON("speed_demon", "闪电答题", "2秒内答对一题", "⚡"),
    DAILY_STREAK_7("daily_streak_7", "坚持不懈", "连续签到7天", "☉"),
    GOLD_RANK("gold_rank", "黄金段位", "达到黄金段位", "●"),
    DIAMOND_RANK("diamond_rank", "钻石段位", "达到钻石段位", "◇"),
    CENTURION("centurion", "积分达人", "累计获得1000积分", "♛");

    private final String key;
    private final String displayName;
    private final String description;
    private final String icon;

    Badge(String key, String displayName, String description, String icon) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String key() { return key; }
    public String displayName() { return displayName; }
    public String description() { return description; }
    public String icon() { return icon; }

    public static Badge fromKey(String key) {
        for (Badge b : values()) {
            if (b.key.equals(key)) return b;
        }
        return null;
    }
}
