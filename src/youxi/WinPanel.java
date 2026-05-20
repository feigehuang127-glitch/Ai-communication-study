package youxi;

import java.awt.*;

import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;

import youxi.animation.AnimationScheduler;
import youxi.animation.Easing;
import youxi.animation.Interpolators;
import youxi.model.User;
import youxi.theme.ThemeColors;
import youxi.ui.AnimatedBackgroundPanel;

public class WinPanel extends AnimatedBackgroundPanel {

    private JLabel resultIcon;
    private JLabel resultTitle;
    private JLabel scoreLabel;
    private JLabel rankLabel;
    private JLabel rankUpLabel;

    public WinPanel() {
        super("picture_pro/Gemini_Generated_Image_9rhprw9rhprw9rhp.png",
              AnimatedBackgroundPanel.OrbTheme.ROSE,
              new Color(0, 0, 0, 80));
        setLayout(new GridBagLayout());
        setName("win");

        JPanel card = createGlassCard();

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.insets = new Insets(8, 0, 8, 0);

        resultIcon = new JLabel("★", SwingConstants.CENTER);
        resultIcon.setFont(new Font("微软雅黑", Font.PLAIN, 72));
        resultIcon.setForeground(ThemeColors.GOLD_GLOW);
        card.add(resultIcon, gc);

        resultTitle = new JLabel("恭喜过关！", SwingConstants.CENTER);
        resultTitle.setFont(new Font("微软雅黑", Font.BOLD, 30));
        resultTitle.setForeground(ThemeColors.GOLD);
        card.add(resultTitle, gc);

        scoreLabel = new JLabel("", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        scoreLabel.setForeground(ThemeColors.TEXT_DARK);
        card.add(scoreLabel, gc);

        rankLabel = new JLabel("", SwingConstants.CENTER);
        rankLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        rankLabel.setForeground(ThemeColors.TEXT_BODY);
        gc.insets = new Insets(2, 0, 2, 0);
        card.add(rankLabel, gc);

        rankUpLabel = new JLabel("", SwingConstants.CENTER);
        rankUpLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        rankUpLabel.setForeground(ThemeColors.GOLD);
        gc.insets = new Insets(8, 0, 24, 0);
        card.add(rankUpLabel, gc);

        JButton backBtn = createNeonButton("返回主菜单");
        backBtn.addActionListener(e -> MainFrame.getInstance().showPanel("home"));
        card.add(backBtn, gc);

        add(card);
    }

    private JPanel createGlassCard() {
        JPanel card = new JPanel();
        card.setBackground(ThemeColors.CARD_BG);
        card.setOpaque(true);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(36, 40, 36, 40)));
        card.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        return card;
    }

    private JButton createNeonButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.BOLD, 19));
        btn.setForeground(ThemeColors.TEXT_WHITE);
        btn.setBackground(ThemeColors.PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(260, 48));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        return btn;
    }

    public void setup(User user, int correctCount, int scoreEarned, boolean comboWin,
                      String rankBefore, String rankAfter) {
        if (comboWin) {
            resultIcon.setText("◆");
            resultIcon.setForeground(ThemeColors.GOLD_GLOW);
            resultTitle.setText("完美连击！");
            resultTitle.setForeground(ThemeColors.GOLD);
        } else {
            resultIcon.setText("★");
            resultIcon.setForeground(ThemeColors.GOLD_GLOW);
            resultTitle.setText("恭喜过关！");
            resultTitle.setForeground(ThemeColors.GOLD);
        }

        scoreLabel.setText("答对 " + correctCount + " 题  |  获得 +" + scoreEarned + " 积分");
        rankLabel.setText("当前段位: " + rankAfter + "  |  总积分: " + user.getTotalScore());

        if (!rankBefore.equals(rankAfter)) {
            rankUpLabel.setText("段位晋升: " + rankBefore + " → " + rankAfter);
            rankUpLabel.setForeground(ThemeColors.GOLD);
        } else {
            rankUpLabel.setText(" ");
        }
        revalidate();
        repaint();

        // entrance: icon and title slide down from above
        AnimationScheduler.getInstance().animate(350, Easing.EASE_OUT_CUBIC, t -> {
            int top = (int) (40 * (1 - t));
            resultIcon.setBorder(BorderFactory.createEmptyBorder(top, 0, 0, 0));
            resultTitle.setBorder(BorderFactory.createEmptyBorder((int)(36 * (1 - t)), 0, 0, 0));
            revalidate();
        }, () -> {
            resultIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            resultTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            revalidate();
        });
    }
}
