package youxi.model;

public class Achievement {
    private int id;
    private int userId;
    private String badgeKey;
    private String earnedAt;

    public Achievement() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getBadgeKey() { return badgeKey; }
    public void setBadgeKey(String badgeKey) { this.badgeKey = badgeKey; }
    public String getEarnedAt() { return earnedAt; }
    public void setEarnedAt(String earnedAt) { this.earnedAt = earnedAt; }

    public Badge getBadge() { return Badge.fromKey(badgeKey); }
}
