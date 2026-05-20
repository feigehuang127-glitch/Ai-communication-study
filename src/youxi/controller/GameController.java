package youxi.controller;

import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import youxi.*;
import youxi.dao.DailyChallengeDAO;
import youxi.model.User;
import youxi.util.Config;

public class GameController {

    private static GameController instance;
    private final MainFrame frame;
    private User currentUser;

    public static GameController getInstance() { return instance; }
    public static void init(MainFrame frame) { instance = new GameController(frame); }

    private GameController(MainFrame frame) {
        this.frame = frame;
    }

    public User getCurrentUser() { return currentUser; }

    // ── Navigation ──

    public void loginSucceeded(User user) {
        this.currentUser = user;
        HomePanel hp = (HomePanel) frame.getPanel("home");
        if (hp != null) hp.setUser(user);
        frame.showPanel("home");
    }

    public void logout() {
        this.currentUser = null;
        Config.set("login.skip_auto", "true");
        frame.showPanel("login");
    }

    public void goToCategory(boolean practiceMode) {
        CategorySelectPanel csp = (CategorySelectPanel) frame.getPanel("category");
        if (csp != null) {
            if (practiceMode) csp.setPracticeMode(currentUser);
            else csp.setUser(currentUser);
        }
        frame.showPanel("category");
    }

    public void goToGame(String category) {
        GamePanel gp = (GamePanel) frame.getPanel("game");
        if (gp != null) gp.startGame(currentUser, category);
        frame.showPanel("game");
    }

    public void goToDailyChallenge() {
        DailyChallengeDAO dao = new DailyChallengeDAO();
        try {
            if (dao.hasCompletedToday(currentUser.getId())) {
                int score = dao.getTodayScore(currentUser.getId());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame,
                            "今日挑战已完成！\n得分：" + score,
                            "每日挑战", JOptionPane.INFORMATION_MESSAGE);
                });
                return;
            }
        } catch (SQLException ignored) {}
        GamePanel gp = (GamePanel) frame.getPanel("game");
        if (gp != null) gp.startDailyChallenge(currentUser);
        frame.showPanel("game");
    }

    public void goToPractice(String category) {
        PracticePanel pp = (PracticePanel) frame.getPanel("practice");
        if (pp != null) pp.startPractice(currentUser, category);
        frame.showPanel("practice");
    }

    public void gameWin(int correctCount, int scoreEarned, boolean comboWin,
                        String rankBefore, String rankAfter) {
        WinPanel wp = (WinPanel) frame.getPanel("win");
        if (wp != null) wp.setup(currentUser, correctCount, scoreEarned, comboWin,
                                  rankBefore, rankAfter);
        frame.showPanel("win");
    }

    public void gameLose(int correctCount, int scoreEarned) {
        LosePanel lp = (LosePanel) frame.getPanel("lose");
        if (lp != null) lp.setup(currentUser, correctCount, scoreEarned);
        frame.showPanel("lose");
    }

    public void goToWrongBook() {
        WrongBookPanel wbp = (WrongBookPanel) frame.getPanel("wrongbook");
        if (wbp != null) {
            wbp.setUser(currentUser);
            wbp.refreshData();
        }
        frame.showPanel("wrongbook");
    }

    public void goToScore() {
        ScorePanel sp = (ScorePanel) frame.getPanel("score");
        if (sp != null) {
            sp.setUser(currentUser);
            sp.refreshData();
        }
        frame.showPanel("score");
    }

    public void goToLeaderboard() {
        LeaderboardPanel lbp = (LeaderboardPanel) frame.getPanel("leaderboard");
        if (lbp != null) {
            lbp.setUser(currentUser);
            lbp.refreshData();
        }
        frame.showPanel("leaderboard");
    }

    public void goToSettings() {
        SettingsPanel stp = (SettingsPanel) frame.getPanel("settings");
        if (stp != null) stp.setUser(currentUser);
        frame.showPanel("settings");
    }

    public void goToHome() {
        HomePanel hp = (HomePanel) frame.getPanel("home");
        if (hp != null) hp.setUser(currentUser);
        frame.showPanel("home");
    }
}
