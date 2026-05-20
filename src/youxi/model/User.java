package youxi.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String role;
    private int totalScore;
    private String rank;
    private String lastCheckinDate;

    public User() {}

    public User(int id, String username, String passwordHash, String role,
                int totalScore, String rank, String lastCheckinDate) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.totalScore = totalScore;
        this.rank = rank;
        this.lastCheckinDate = lastCheckinDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public String getLastCheckinDate() { return lastCheckinDate; }
    public void setLastCheckinDate(String lastCheckinDate) { this.lastCheckinDate = lastCheckinDate; }
}
