package youxi;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;

import youxi.animation.AnimationScheduler;
import youxi.animation.Easing;
import youxi.dao.UserDAO;
import youxi.dao.UserDAO.LeaderboardEntry;
import youxi.model.User;
import youxi.theme.ThemeColors;
import youxi.ui.AnimatedBackgroundPanel;

public class LeaderboardPanel extends AnimatedBackgroundPanel {

    private User user;
    private JPanel contentPanel;
    private JLabel loadingLabel;
    private final UserDAO userDAO = new UserDAO();

    public LeaderboardPanel() {
        super("picture_pro/Gemini_Generated_Image_m0qrvem0qrvem0qr.png",
              AnimatedBackgroundPanel.OrbTheme.BLUE,
              new Color(0, 0, 0, 75));
        setLayout(new BorderLayout());
        setName("leaderboard");

        add(createTopBar(), BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(8, 24, 16, 24));

        loadingLabel = new JLabel("加载中...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        loadingLabel.setForeground(ThemeColors.TEXT_MUTED);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(loadingLabel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = scrollPane.getViewport().getWidth();
                if (w > 0) contentPanel.setPreferredSize(new Dimension(w, contentPanel.getPreferredSize().height));
                contentPanel.revalidate();
            }
        });
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void refreshData() {
        loadingLabel.setText("加载中...");
        loadingLabel.setVisible(true);
        new Thread(() -> {
            try {
                List<LeaderboardEntry> list = userDAO.getTopPlayers(20);
                SwingUtilities.invokeLater(() -> renderList(list));
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> loadingLabel.setText("加载失败"));
            }
        }).start();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));

        JLabel title = new JLabel("排行榜");
        title.setFont(new Font("微软雅黑", Font.BOLD, 30));
        title.setForeground(ThemeColors.TEXT_DARK);

        JButton backBtn = new JButton("返回");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> MainFrame.getInstance().showPanel("home"));

        bar.add(title, BorderLayout.WEST);
        bar.add(backBtn, BorderLayout.EAST);
        return bar;
    }

    private void renderList(List<LeaderboardEntry> list) {
        loadingLabel.setVisible(false);
        contentPanel.removeAll();

        if (list.isEmpty()) {
            JLabel empty = new JLabel("暂无数据", SwingConstants.CENTER);
            empty.setFont(new Font("微软雅黑", Font.PLAIN, 18));
            empty.setForeground(ThemeColors.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(empty);
        } else {
            for (int i = 0; i < list.size(); i++) {
                JPanel row = createRow(list.get(i), i + 1);
                row.putClientProperty("entranceY", 18);
                contentPanel.add(row);
                contentPanel.add(Box.createVerticalStrut(6));

                final int idx = i;
                javax.swing.Timer delay = new javax.swing.Timer(idx * 40, e -> {
                    AnimationScheduler.getInstance().animate(250, Easing.EASE_OUT_BACK, t -> {
                        row.putClientProperty("entranceY", (int) (18 * (1 - t)));
                        row.repaint();
                    });
                });
                delay.setRepeats(false);
                delay.start();
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createRow(LeaderboardEntry entry, int rank) {
        boolean isMe = user != null && user.getUsername().equals(entry.username);

        JPanel row = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Integer entranceY = (Integer) getClientProperty("entranceY");
                if (entranceY != null && entranceY != 0) {
                    g2.translate(0, entranceY);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setBackground(isMe ? ThemeColors.OPTION_SELECTED : ThemeColors.CARD_BG);
        row.setOpaque(true);
        row.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        row.setPreferredSize(new Dimension(250, 60));
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        // Rank badge
        String rankIcon;
        Color rankColor;
        if (rank == 1) { rankIcon = "#1"; rankColor = ThemeColors.GOLD; }
        else if (rank == 2) { rankIcon = "#2"; rankColor = new Color(0xC0, 0xC0, 0xC0); }
        else if (rank == 3) { rankIcon = "#3"; rankColor = new Color(0xCD, 0x7F, 0x32); }
        else { rankIcon = String.valueOf(rank); rankColor = ThemeColors.TEXT_MUTED; }

        JLabel rankLabel = new JLabel(rankIcon);
        rankLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        rankLabel.setForeground(rankColor);
        rankLabel.setPreferredSize(new Dimension(50, 32));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(rankLabel);

        JLabel nameLabel = new JLabel(entry.username);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        nameLabel.setForeground(isMe ? ThemeColors.PRIMARY : ThemeColors.TEXT_DARK);
        left.add(nameLabel);

        if (isMe) {
            JLabel meBadge = new JLabel("我");
            meBadge.setFont(new Font("微软雅黑", Font.BOLD, 14));
            meBadge.setForeground(ThemeColors.TEXT_WHITE);
            meBadge.setBackground(ThemeColors.PRIMARY);
            meBadge.setOpaque(true);
            meBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            meBadge.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
            left.add(meBadge);
        }

        row.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel rankName = new JLabel(entry.rank);
        rankName.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        rankName.setForeground(ThemeColors.TEXT_MUTED);

        JLabel scoreLabel = new JLabel(entry.totalScore + " 分");
        scoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        scoreLabel.setForeground(ThemeColors.GOLD);

        right.add(rankName);
        right.add(scoreLabel);
        row.add(right, BorderLayout.EAST);

        return row;
    }
}
