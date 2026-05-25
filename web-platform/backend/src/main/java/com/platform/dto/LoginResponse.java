package com.platform.dto;

public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private int totalScore;
    private String rank;

    public LoginResponse(String token, String username, String role, int totalScore, String rank) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.totalScore = totalScore;
        this.rank = rank;
    }

    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public int getTotalScore() { return totalScore; }
    public String getRank() { return rank; }
}
