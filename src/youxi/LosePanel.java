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

public class LosePanel extends AnimatedBackgroundPanel {

    private JLabel correctLabel;
    private JLabel scoreLabel;
    private JLabel tipLabel;

    public LosePanel() {
        super("picture_pro/Gemini_Generated_Image_shxn6kshxn6kshxn.png",
              AnimatedBackgroundPanel.OrbTheme.VIOLET,
              new Color(0, 0, 0, 85));
        setLayout(new GridBagLayout());
        setName("lose");

        JPanel card = createGlassCard();

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.insets = new Insets(8, 0, 8, 0);

        JLabel icon = new JLabel("◆", SwingConstants.CENTER);
        icon.setFont(new Font("微软雅黑", Font.PLAIN, 64));
        icon.setForeground(ThemeColors.ROSE);
        card.add(icon, gc);

        JLabel title = new JLabel("挑战失败", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 30));
        title.setForeground(ThemeColors.DANGER);
        card.add(title, gc);

        correctLabel = new JLabel("", SwingConstants.CENTER);
        correctLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        correctLabel.setForeground(ThemeColors.TEXT_DARK);
        card.add(correctLabel, gc);

        scoreLabel = new JLabel("", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        scoreLabel.setForeground(ThemeColors.TEXT_BODY);
        gc.insets = new Insets(2, 0, 2, 0);
        card.add(scoreLabel, gc);

        tipLabel = new JLabel("再接再厉，多在训练模式中练习吧！", SwingConstants.CENTER);
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        tipLabel.setForeground(ThemeColors.TEXT_MUTED);
        gc.insets = new Insets(8, 0, 24, 0);
        card.add(tipLabel, gc);

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));

        JButton retryBtn = new JButton("再试一次");
        retryBtn.setFont(new Font("微软雅黑", Font.BOLD, 19));
        retryBtn.setForeground(ThemeColors.TEXT_WHITE);
        retryBtn.setBackground(ThemeColors.PRIMARY);
        retryBtn.setFocusPainted(false);
        retryBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        retryBtn.setMaximumSize(new Dimension(260, 48));
        retryBtn.setPreferredSize(new Dimension(260, 48));
        retryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        retryBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        retryBtn.addActionListener(e -> MainFrame.getInstance().showPanel("category"));
        btnPanel.add(retryBtn);
        btnPanel.add(Box.createVerticalStrut(8));

        JButton backBtn = new JButton("返回主菜单");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        backBtn.setForeground(ThemeColors.TEXT_MUTED);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setMaximumSize(new Dimension(260, 40));
        backBtn.setPreferredSize(new Dimension(260, 40));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        backBtn.addActionListener(e -> MainFrame.getInstance().showPanel("home"));
        btnPanel.add(backBtn);

        gc.insets = new Insets(0, 0, 0, 0);
        card.add(btnPanel, gc);

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

    public void setup(User user, int correctCount, int scoreEarned) {
        correctLabel.setText("答对 " + correctCount + " / 10 题  |  积分: " + scoreEarned);
        scoreLabel.setText("当前段位: " + user.getRank() + "  |  总积分: " + user.getTotalScore());
        revalidate();

        // entrance: labels slide up from below
        AnimationScheduler.getInstance().animate(300, Easing.EASE_OUT_CUBIC, t -> {
            int bottom = (int) (30 * (1 - t));
            correctLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, bottom, 0));
            scoreLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, (int) (28 * (1 - t)), 0));
            revalidate();
        }, () -> {
            correctLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            scoreLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            revalidate();
        });
    }
}
