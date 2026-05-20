package youxi;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;

import youxi.controller.GameController;
import youxi.dao.AchievementDAO;
import youxi.model.Achievement;
import youxi.model.Badge;
import youxi.model.User;
import youxi.service.UserService;
import youxi.service.UserService.CheckinResult;
import youxi.theme.ThemeColors;
import youxi.ui.AnimatedBackgroundPanel;
import youxi.ui.HoverCard;

public class HomePanel extends AnimatedBackgroundPanel {

    private User user;
    private JLabel userLabel;
    private JLabel rankLabel;
    private JPanel checkinPill;
    private JLabel checkinIcon;
    private JLabel checkinText;
    private boolean checkedInToday;
    private int checkinStreak;
    private JPanel badgeStrip;

    private final UserService userService = new UserService();
    private final AchievementDAO achievementDAO = new AchievementDAO();

    public HomePanel() {
        super("picture_pro/Gemini_Generated_Image_9rhprw9rhprw9rhp.png",
              AnimatedBackgroundPanel.OrbTheme.AMBER,
              new Color(0, 0, 0, 30));
        setLayout(new GridBagLayout());

        add(createUserBar(), createTopConstraints());
        add(createMenuPanel(), createCenterConstraints());
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            userLabel.setText(user.getUsername());
            rankLabel.setText("段位: " + user.getRank());
            refreshCheckinStatus();
            loadBadges();
        }
    }

    private void refreshCheckinStatus() {
        if (user == null) return;
        new Thread(() -> {
            try {
                boolean today = userService.hasCheckedInToday(user.getId());
                int streak = today ? userService.getStreak(user.getId()) : 0;
                SwingUtilities.invokeLater(() -> {
                    checkedInToday = today;
                    checkinStreak = streak;
                    updateCheckinUI();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void drawOverlay(Graphics2D g2, int w, int h) {
        super.drawOverlay(g2, w, h);
        // 从顶部暗色渐变到中间透明，底部微暗，制造沉浸感
        GradientPaint topGrad = new GradientPaint(0, 0, new Color(0, 0, 0, 80),
                0, h * 0.45f, new Color(0, 0, 0, 0));
        g2.setPaint(topGrad);
        g2.fillRect(0, 0, w, h);

        GradientPaint bottomGrad = new GradientPaint(0, h * 0.6f, new Color(0, 0, 0, 0),
                0, h, new Color(0, 0, 0, 60));
        g2.setPaint(bottomGrad);
        g2.fillRect(0, 0, w, h);
    }

    private GridBagConstraints createTopConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(20, 20, 0, 20);
        return gbc;
    }

    private GridBagConstraints createCenterConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 40, 40, 40);
        return gbc;
    }

    private JPanel createUserBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        userLabel = new JLabel(" ");
        userLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        userLabel.setForeground(ThemeColors.TEXT_WHITE);

        rankLabel = new JLabel(" ");
        rankLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        rankLabel.setForeground(new Color(255, 255, 255, 200));

        info.add(userLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(rankLabel);
        info.add(Box.createVerticalStrut(6));
        badgeStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgeStrip.setOpaque(false);
        info.add(badgeStrip);

        bar.add(info, BorderLayout.WEST);
        bar.add(createCheckinPill(), BorderLayout.EAST);
        return bar;
    }

    private JPanel createCheckinPill() {
        checkinPill = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        checkinPill.setOpaque(false);
        checkinPill.setCursor(new Cursor(Cursor.HAND_CURSOR));

        checkinIcon = new JLabel("◆");
        checkinIcon.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        checkinIcon.setForeground(ThemeColors.CHECKIN_COLOR);

        checkinText = new JLabel("签到打卡");
        checkinText.setFont(new Font("微软雅黑", Font.BOLD, 17));
        checkinText.setForeground(ThemeColors.CHECKIN_COLOR);

        checkinPill.add(checkinIcon);
        checkinPill.add(checkinText);

        checkinPill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!checkedInToday) doCheckin();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!checkedInToday) {
                    checkinPill.setOpaque(true);
                    checkinPill.setBackground(new Color(255, 255, 255, 40));
                    checkinPill.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                checkinPill.setOpaque(false);
                checkinPill.repaint();
            }
        });

        return checkinPill;
    }

    private void updateCheckinUI() {
        if (checkedInToday) {
            checkinIcon.setText("✓");
            checkinText.setText("已打卡 · 连续 " + checkinStreak + " 天");
            checkinText.setForeground(ThemeColors.CHECKIN_DONE);
            checkinPill.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            checkinPill.setOpaque(false);
        } else {
            checkinIcon.setText("◆");
            checkinText.setText("签到打卡");
            checkinText.setForeground(ThemeColors.CHECKIN_COLOR);
            checkinPill.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    private void doCheckin() {
        if (checkedInToday || user == null) return;
        checkinText.setText("签到中...");
        checkinPill.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                CheckinResult result = userService.checkin(user);
                SwingUtilities.invokeLater(() -> {
                    checkedInToday = true;
                    checkinStreak = result.streak;
                    rankLabel.setText("段位: " + user.getRank() + "  |  积分: " + user.getTotalScore());
                    updateCheckinUI();

                    JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(HomePanel.this),
                            "打卡成功！\n\n+1 积分\n连续打卡 " + result.streak + " 天\n当前总积分: " + result.totalScore,
                            "每日打卡",
                            JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    updateCheckinUI();
                    JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(HomePanel.this),
                            "打卡失败，请稍后重试",
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private JPanel createMenuPanel() {
        JPanel menu = new JPanel();
        menu.setLayout(new GridLayout(4, 2, 14, 14));
        menu.setBackground(ThemeColors.GLASS_BG);
        menu.setOpaque(true);
        menu.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        menu.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        String[][] items = {
            {"每日挑战", "每天一次，双倍积分"},
            {"开始游戏", "随机出题，限时计分"},
            {"训练模式", "分类练习，稳步提升"},
            {"错题本",   "复习曾经做错的题"},
            {"积分和段位", "积分记录与段位晋升"},
            {"排行榜",   "全服积分排名"},
            {"选项",     "偏好与个人信息"},
            {"退出游戏", "退出当前账号"},
        };

        java.util.List<JPanel> menuItems = new java.util.ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            JPanel item = createMenuItem(items[i][0], items[i][1], ThemeColors.SUBJECT_COLORS[i][0], ThemeColors.SUBJECT_COLORS[i][1], items[i][0].replaceAll("[^\\u4e00-\\u9fff]", ""));
            menuItems.add(item);
            menu.add(item);
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        wrapper.add(menu, BorderLayout.CENTER);

        Runnable updateSizes = () -> {
            int w = wrapper.getWidth() - 72 - 14; // menu insets + 1 gap
            if (w < 200) w = 200;
            int itemW = w / 2;
            Dimension d = new Dimension(itemW, 88);
            for (JPanel item : menuItems) {
                item.setPreferredSize(d);
            }
            menu.revalidate();
        };
        wrapper.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) { updateSizes.run(); }
        });
        SwingUtilities.invokeLater(updateSizes);

        return wrapper;
    }

    private JPanel createMenuItem(String text, String subtitle, Color color, Color colorDark, String action) {
        // darken is just color * 0.88
        Color idle = new Color(
                Math.max(0, (int) (color.getRed()   * 0.88f)),
                Math.max(0, (int) (color.getGreen() * 0.88f)),
                Math.max(0, (int) (color.getBlue()  * 0.88f)),
                color.getAlpha());
        HoverCard item = new HoverCard(idle, color);
        item.setLayout(new BorderLayout());
        item.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        item.setPreferredSize(new Dimension(420, 88));
        item.setMinimumSize(new Dimension(200, 88));
        item.setMaximumSize(new Dimension(Short.MAX_VALUE, 88));
        item.setOnClick(() -> handleMenuAction(action));

        JLabel title = new JLabel(text);
        title.setFont(new Font("微软雅黑", Font.BOLD, 20));
        title.setForeground(ThemeColors.TEXT_WHITE);

        JLabel desc = new JLabel(subtitle);
        desc.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        desc.setForeground(new Color(255, 255, 255, 180));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(title);
        leftPanel.add(Box.createVerticalStrut(2));
        leftPanel.add(desc);

        item.add(leftPanel, BorderLayout.CENTER);
        return item;
    }

    private void loadBadges() {
        if (user == null) return;
        new Thread(() -> {
            try {
                java.util.List<Achievement> list = achievementDAO.findByUserId(user.getId());
                SwingUtilities.invokeLater(() -> renderBadges(list));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void renderBadges(java.util.List<Achievement> list) {
        badgeStrip.removeAll();
        java.util.Set<String> earned = new java.util.HashSet<>();
        for (Achievement a : list) earned.add(a.getBadgeKey());

        for (Badge badge : Badge.values()) {
            boolean has = earned.contains(badge.key());
            JLabel lbl = new JLabel(badge.icon() + (has ? "" : " /锁"));
            lbl.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            lbl.setToolTipText(has ? badge.displayName() + ": " + badge.description()
                                  : "??? (未解锁)");
            if (!has) lbl.setForeground(new Color(255, 255, 255, 80));
            badgeStrip.add(lbl);
        }
        badgeStrip.revalidate();
        badgeStrip.repaint();
    }

    private void handleMenuAction(String action) {
        GameController gc = GameController.getInstance();
        switch (action) {
            case "每日挑战":  gc.goToDailyChallenge(); break;
            case "开始游戏":  gc.goToCategory(false); break;
            case "训练模式":  gc.goToCategory(true);  break;
            case "错题本":    gc.goToWrongBook();     break;
            case "积分和段位": gc.goToScore();        break;
            case "排行榜":    gc.goToLeaderboard();   break;
            case "选项":      gc.goToSettings();      break;
            case "退出游戏":
                user = null;
                userLabel.setText(" ");
                rankLabel.setText(" ");
                gc.logout();
                break;
        }
    }
}
