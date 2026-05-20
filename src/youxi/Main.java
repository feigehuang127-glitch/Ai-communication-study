package youxi;

import javax.swing.*;

import youxi.controller.GameController;
import youxi.dao.AchievementDAO;
import youxi.dao.DailyChallengeDAO;
import youxi.service.QuestionCache;
import youxi.util.SoundManager;

public class Main {
    public static void main(String[] args) {
        // 后台预加载
        new Thread(() -> QuestionCache.getInstance()).start();
        new Thread(SoundManager::init).start();
        new Thread(AchievementDAO::ensureTable).start();
        new Thread(() -> new DailyChallengeDAO().ensureTable()).start();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            GameController.init(frame);
            frame.addPanel(new LoginPanel(), "login");
            frame.addPanel(new HomePanel(), "home");
            frame.addPanel(new CategorySelectPanel(), "category");
            frame.addPanel(new GamePanel(), "game");
            frame.addPanel(new WinPanel(), "win");
            frame.addPanel(new LosePanel(), "lose");
            frame.addPanel(new PracticePanel(), "practice");
            frame.addPanel(new WrongBookPanel(), "wrongbook");
            frame.addPanel(new ScorePanel(), "score");
            frame.addPanel(new SettingsPanel(), "settings");
            frame.addPanel(new LeaderboardPanel(), "leaderboard");
            frame.showPanel("login");
            frame.setVisible(true);
        });
    }
}
