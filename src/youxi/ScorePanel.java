package youxi;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;

import youxi.dao.GameHistoryDAO;
import youxi.dao.GameHistoryDAO.GameStats;
import youxi.model.GameHistory;
import youxi.model.User;
import youxi.service.UserService;
import youxi.service.UserService.RankTier;

import youxi.theme.ThemeColors;
import youxi.ui.AnimatedBackgroundPanel;

public class ScorePanel extends AnimatedBackgroundPanel {

    private User user;
    private JPanel contentPanel;
    private JLabel loadingLabel;

    private final GameHistoryDAO gameHistoryDAO = new GameHistoryDAO();

    public ScorePanel() {
        super("picture_pro/Gemini_Generated_Image_yr22rgyr22rgyr22.png",
              AnimatedBackgroundPanel.OrbTheme.INDIGO,
              new Color(0, 0, 0, 75));
        setLayout(new BorderLayout());
        setName("score");

        add(createHeader(), BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(8, 24, 24, 24));

        loadingLabel = new JLabel("加载中...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        loadingLabel.setForeground(ThemeColors.TEXT_MUTED);
        loadingLabel.setVisible(false);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(loadingLabel, BorderLayout.NORTH);
        centerWrapper.add(contentPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        add(createBottomBar(), BorderLayout.SOUTH);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void refreshData() {
        if (user == null) return;

        contentPanel.removeAll();
        loadingLabel.setVisible(true);
        contentPanel.revalidate();
        contentPanel.repaint();

        new Thread(() -> {
            try {
                GameStats stats = gameHistoryDAO.getStats(user.getId());
                List<GameHistory> history = gameHistoryDAO.findByUserId(user.getId(), 15);

                SwingUtilities.invokeLater(() -> {
                    contentPanel.removeAll();
                    loadingLabel.setVisible(false);

                    contentPanel.add(createScoreOverview());
                    contentPanel.add(Box.createVerticalStrut(14));
                    contentPanel.add(createRankLadder());
                    contentPanel.add(Box.createVerticalStrut(14));
                    contentPanel.add(createScoringRules());
                    contentPanel.add(Box.createVerticalStrut(14));
                    contentPanel.add(createStatsRow(stats));
                    contentPanel.add(Box.createVerticalStrut(20));
                    contentPanel.add(createHistoryTitle());
                    contentPanel.add(Box.createVerticalStrut(8));

                    if (history.isEmpty()) {
                        contentPanel.add(createEmptyHistory());
                    } else {
                        for (int i = 0; i < history.size(); i++) {
                            contentPanel.add(createHistoryCard(history.get(i)));
                            if (i < history.size() - 1) {
                                contentPanel.add(Box.createVerticalStrut(8));
                            }
                        }
                    }

                    contentPanel.revalidate();
                    contentPanel.repaint();
                });
            } catch (SQLException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    loadingLabel.setVisible(false);
                    showError("加载失败，请稍后重试");
                });
            }
        }).start();
    }

    private void showError(String msg) {
        JLabel error = new JLabel(msg, SwingConstants.CENTER);
        error.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        error.setForeground(ThemeColors.TEXT_MUTED);
        contentPanel.add(error);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ============================================================
    // Header
    // ============================================================

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(24, 24, 8, 24));

        JLabel title = new JLabel("积分和段位");
        title.setFont(new Font("微软雅黑", Font.BOLD, 28));
        title.setForeground(ThemeColors.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("查看积分记录与段位晋升");
        subtitle.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        subtitle.setForeground(ThemeColors.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);

        return header;
    }

    // ============================================================
    // Score Overview — prominent score display
    // ============================================================

    private JPanel createScoreOverview() {
        String rank = user.getRank();
        int score = user.getTotalScore();
        RankTier tier = UserService.getRankTier(rank);
        int rankIndex = tier.index;

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeColors.CARD_BG);
        card.setOpaque(true);
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        card.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        JLabel iconLabel = new JLabel(tier.icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("微软雅黑", Font.PLAIN, 72));
        iconLabel.setForeground(tier.color);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel rankLabel = new JLabel(rank, SwingConstants.CENTER);
        rankLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        rankLabel.setForeground(tier.color);
        rankLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel(String.format("%,d", score), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 56));
        scoreLabel.setForeground(ThemeColors.TEXT_DARK);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreUnit = new JLabel("总积分", SwingConstants.CENTER);
        scoreUnit.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        scoreUnit.setForeground(ThemeColors.TEXT_MUTED);
        scoreUnit.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(rankLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(scoreLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(scoreUnit);

        java.util.List<RankTier> tiers = UserService.getRankTiers();
        if (rankIndex < tiers.size() - 1) {
            RankTier next = tiers.get(rankIndex + 1);
            int currentMin = tier.minScore;
            int nextMin = next.minScore;
            int range = nextMin - currentMin;
            int progress = score - currentMin;
            int remaining = nextMin - score;
            double pct = Math.min(1.0, Math.max(0.0, (double) progress / range));

            card.add(Box.createVerticalStrut(20));

            JPanel progressRow = new JPanel(new BorderLayout());
            progressRow.setOpaque(false);
            progressRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

            JLabel progressLabel = new JLabel("距 " + next.icon + " " + next.name + " 还差 " + String.format("%,d", remaining) + " 积分");
            progressLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
            progressLabel.setForeground(ThemeColors.TEXT_MUTED);

            JLabel pctLabel = new JLabel((int) (pct * 100) + "%");
            pctLabel.setFont(new Font("微软雅黑", Font.BOLD, 17));
            pctLabel.setForeground(next.color);

            progressRow.add(progressLabel, BorderLayout.WEST);
            progressRow.add(pctLabel, BorderLayout.EAST);
            card.add(progressRow);

            card.add(Box.createVerticalStrut(8));

            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue((int) (pct * 100));
            bar.setPreferredSize(new Dimension(Short.MAX_VALUE, 22));
            bar.setMaximumSize(new Dimension(Short.MAX_VALUE, 22));
            bar.setForeground(next.color);
            bar.putClientProperty(FlatClientProperties.STYLE, "arc: 11");
            bar.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(bar);
        } else {
            card.add(Box.createVerticalStrut(16));
            JLabel maxLabel = new JLabel("已达最高段位！", SwingConstants.CENTER);
            maxLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
            maxLabel.setForeground(ThemeColors.WARNING);
            maxLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(maxLabel);
        }

        return card;
    }

    // ============================================================
    // Rank Ladder — all ranks with thresholds
    // ============================================================

    private JPanel createRankLadder() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(ThemeColors.CARD_BG);
        outer.setOpaque(true);
        outer.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        outer.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        outer.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        JLabel title = new JLabel("段位晋升体系");
        title.setFont(new Font("微软雅黑", Font.BOLD, 20));
        title.setForeground(ThemeColors.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(title);
        outer.add(Box.createVerticalStrut(14));

        java.util.List<RankTier> tiers = UserService.getRankTiers();
        int currentRankIndex = UserService.getRankIndex(user.getRank());
        String[] descs = {"初入江湖", "小有所成", "渐入佳境", "融会贯通", "炉火纯青", "登峰造极", "出神入化", "至尊荣耀"};

        for (int i = tiers.size() - 1; i >= 0; i--) {
            RankTier t = tiers.get(i);
            Color rankColor = t.color;
            boolean isCurrent = (i == currentRankIndex);

            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(isCurrent);
            row.setMaximumSize(new Dimension(Short.MAX_VALUE, 52));
            row.setPreferredSize(new Dimension(Short.MAX_VALUE, 52));

            if (isCurrent) {
                row.setBackground(new Color(rankColor.getRed(), rankColor.getGreen(),
                        rankColor.getBlue(), 20));
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 5, 0, 0, rankColor),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)));
            } else {
                row.setBorder(BorderFactory.createEmptyBorder(8, 21, 8, 16));
            }

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            left.setOpaque(false);

            JLabel icon = new JLabel(t.icon);
            icon.setFont(new Font("微软雅黑", Font.PLAIN, 28));
            icon.setForeground(t.color);

            JLabel name = new JLabel(t.name);
            name.setFont(new Font("微软雅黑", isCurrent ? Font.BOLD : Font.PLAIN, 16));
            name.setForeground(isCurrent ? rankColor : ThemeColors.TEXT_DARK);

            JLabel desc = new JLabel("· " + descs[i]);
            desc.setFont(new Font("微软雅黑", Font.PLAIN, 19));
            desc.setForeground(ThemeColors.TEXT_MUTED);

            left.add(icon);
            left.add(name);
            left.add(desc);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            right.setOpaque(false);

            String rangeText;
            if (i == tiers.size() - 1) {
                rangeText = "≥ " + String.format("%,d", t.minScore);
            } else {
                rangeText = String.format("%,d", t.minScore) + " – " + String.format("%,d", tiers.get(i + 1).minScore - 1);
            }

            JLabel range = new JLabel(rangeText);
            range.setFont(new Font("微软雅黑", Font.PLAIN, 20));
            range.setForeground(isCurrent ? rankColor : ThemeColors.TEXT_MUTED);

            if (isCurrent) {
                JLabel tag = new JLabel(" ◀ 当前");
                tag.setFont(new Font("微软雅黑", Font.BOLD, 19));
                tag.setForeground(rankColor);
                right.add(tag);
            }
            right.add(range);

            row.add(left, BorderLayout.WEST);
            row.add(right, BorderLayout.EAST);

            outer.add(row);

            if (i > 0) {
                JSeparator sep = new JSeparator();
                sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
                sep.setForeground(new Color(0xE0, 0xE0, 0xE0));
                outer.add(sep);
            }
        }

        return outer;
    }

    // ============================================================
    // Scoring Rules
    // ============================================================

    private JPanel createScoringRules() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeColors.CARD_BG);
        card.setOpaque(true);
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        card.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        JLabel title = new JLabel("积分规则");
        title.setFont(new Font("微软雅黑", Font.BOLD, 20));
        title.setForeground(ThemeColors.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(14));

        Object[][] rules = {
            {"✓", "答对 1 题", "+10 积分", ThemeColors.SUCCESS},
            {"◆", "连对 5 题（完美连击）", "额外 +30 积分", ThemeColors.WARNING},
            {"▲", "青铜段位加成", "积分 ×1.5", new Color(0x8D, 0x6E, 0x63)},
            {"▲", "白银段位加成", "积分 ×1.3", new Color(0x90, 0xA4, 0xAE)},
            {"▲", "黄金段位加成", "积分 ×1.1", ThemeColors.WARNING},
            {"○", "每日首次登录打卡", "+1 积分", ThemeColors.PRIMARY},
            {"◈", "青铜/白银失败保护", "失败不扣分", ThemeColors.SUCCESS},
            {"✗", "失败", "-1 积分", ThemeColors.DANGER},
            {"◆", "段位晋升", "达到阈值自动升段", ThemeColors.PURPLE},
        };

        for (int i = 0; i < rules.length; i++) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
            row.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);

            String iconStr = (String) rules[i][0];
            String descStr = (String) rules[i][1];
            String valueStr = (String) rules[i][2];
            Color ruleColor = (Color) rules[i][3];

            JLabel icon = new JLabel(iconStr);
            icon.setFont(new Font("微软雅黑", Font.PLAIN, 20));
            icon.setForeground(ruleColor);

            JLabel desc = new JLabel(descStr);
            desc.setFont(new Font("微软雅黑", Font.PLAIN, 18));
            desc.setForeground(ThemeColors.TEXT_DARK);

            left.add(icon);
            left.add(desc);

            JLabel value = new JLabel(valueStr);
            value.setFont(new Font("微软雅黑", Font.BOLD, 30));
            value.setForeground(ruleColor);

            row.add(left, BorderLayout.WEST);
            row.add(value, BorderLayout.EAST);

            card.add(row);

            if (i < rules.length - 1) {
                JSeparator sep = new JSeparator();
                sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
                sep.setForeground(new Color(0xF0, 0xF0, 0xF0));
                card.add(sep);
            }
        }

        return card;
    }

    // ============================================================
    // Stats Row
    // ============================================================

    private JPanel createStatsRow(GameStats stats) {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 90));

        row.add(createStatCard("总场次", String.valueOf(stats.totalGames), ThemeColors.PRIMARY,
                stats.totalGames > 0 ? "勤奋练习！" : "快去挑战"));
        row.add(createStatCard("胜利", String.valueOf(stats.wins), ThemeColors.SUCCESS,
                stats.comboWins > 0 ? "含 " + stats.comboWins + " 次连击" : ""));
        row.add(createStatCard("失败", String.valueOf(stats.losses), ThemeColors.DANGER,
                "再接再厉"));
        int winRate = stats.totalGames > 0
                ? (int) Math.round(stats.wins * 100.0 / stats.totalGames) : 0;
        row.add(createStatCard("胜率", winRate + "%", ThemeColors.WARNING,
                stats.totalGames > 0 ? getWinRateAdvice(winRate) : ""));

        return row;
    }

    private JPanel createStatCard(String label, String value, Color color, String footer) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeColors.CARD_BG);
        card.setOpaque(true);
        card.setBorder(BorderFactory.createEmptyBorder(14, 12, 10, 12));
        card.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        JLabel titleLabel = new JLabel(label, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        titleLabel.setForeground(ThemeColors.TEXT_MUTED);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLabel);

        if (footer != null && !footer.isEmpty()) {
            card.add(Box.createVerticalStrut(4));
            JLabel footerLabel = new JLabel(footer, SwingConstants.CENTER);
            footerLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            footerLabel.setForeground(ThemeColors.TEXT_MUTED);
            footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(footerLabel);
        }

        // Fill remaining space
        card.add(Box.createVerticalGlue());

        return card;
    }

    // ============================================================
    // History
    // ============================================================

    private JPanel createHistoryTitle() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

        JLabel title = new JLabel("最近游戏记录");
        title.setFont(new Font("微软雅黑", Font.BOLD, 20));
        title.setForeground(ThemeColors.TEXT_DARK);

        row.add(title, BorderLayout.WEST);
        return row;
    }

    private JPanel createEmptyHistory() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeColors.CARD_BG);
        card.setOpaque(true);
        card.setBorder(BorderFactory.createEmptyBorder(30, 24, 30, 24));
        card.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        JLabel icon = new JLabel("▣", SwingConstants.CENTER);
        icon.setFont(new Font("微软雅黑", Font.PLAIN, 72));
        icon.setForeground(ThemeColors.TEXT_MUTED);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel text = new JLabel("还没有游戏记录", SwingConstants.CENTER);
        text.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        text.setForeground(ThemeColors.TEXT_MUTED);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("快去挑战吧！", SwingConstants.CENTER);
        hint.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        hint.setForeground(new Color(0xBB, 0xBB, 0xBB));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(icon);
        card.add(Box.createVerticalStrut(12));
        card.add(text);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);

        return card;
    }

    private JPanel createHistoryCard(GameHistory gh) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeColors.CARD_BG);
        card.setOpaque(true);
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        card.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        boolean isWin = "win".equals(gh.getResult()) || "win_combo".equals(gh.getResult());
        boolean isCombo = "win_combo".equals(gh.getResult());
        String resultIcon = isCombo ? "◆" : (isWin ? "✓" : "✗");
        String resultText = isCombo ? "完美连击" : (isWin ? "胜利" : "失败");
        Color resultColor = isCombo ? ThemeColors.WARNING : (isWin ? ThemeColors.SUCCESS : ThemeColors.DANGER);

        // Top row
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel leftInfo = new JPanel();
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));
        leftInfo.setOpaque(false);

        String playedAt = gh.getPlayedAt();
        if (playedAt != null && playedAt.length() > 16) {
            playedAt = playedAt.substring(0, 16).replace("T", " ");
        }

        JLabel dateLabel = new JLabel(playedAt != null ? playedAt : "");
        dateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        dateLabel.setForeground(ThemeColors.TEXT_MUTED);

        JLabel catLabel = new JLabel(gh.getCategory() != null ? gh.getCategory() : "");
        catLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        catLabel.setForeground(ThemeColors.TEXT_DARK);

        leftInfo.add(dateLabel);
        leftInfo.add(Box.createVerticalStrut(2));
        leftInfo.add(catLabel);

        topRow.add(leftInfo, BorderLayout.WEST);

        JPanel badge = new JPanel();
        badge.setOpaque(true);
        badge.setBackground(new Color(resultColor.getRed(), resultColor.getGreen(),
                resultColor.getBlue(), 25));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        badge.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        JLabel badgeLabel = new JLabel(resultIcon + " " + resultText);
        badgeLabel.setFont(new Font("微软雅黑", Font.BOLD, 17));
        badgeLabel.setForeground(resultColor);
        badge.add(badgeLabel);

        topRow.add(badge, BorderLayout.EAST);

        card.add(topRow);
        card.add(Box.createVerticalStrut(10));

        // Bottom row
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        bottomRow.setOpaque(false);

        JLabel correctLabel = new JLabel("答对 " + gh.getCorrectCount() + " 题");
        correctLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        correctLabel.setForeground(ThemeColors.TEXT_DARK);
        bottomRow.add(correctLabel);

        String scoreText = gh.getScoreEarned() >= 0
                ? "+" + gh.getScoreEarned() : String.valueOf(gh.getScoreEarned());
        JLabel earnedLabel = new JLabel(scoreText + " 分");
        earnedLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        earnedLabel.setForeground(gh.getScoreEarned() >= 0 ? ThemeColors.SUCCESS : ThemeColors.DANGER);
        bottomRow.add(earnedLabel);

        String rankBefore = gh.getRankBefore();
        String rankAfter = gh.getRankAfter();
        if (rankBefore != null && rankAfter != null && !rankBefore.equals(rankAfter)) {
            JLabel rankChange = new JLabel(rankBefore + " → " + rankAfter + " ⬆");
            rankChange.setFont(new Font("微软雅黑", Font.BOLD, 17));
            rankChange.setForeground(ThemeColors.WARNING);
            bottomRow.add(rankChange);
        }

        card.add(bottomRow);

        return card;
    }

    // ============================================================
    // Bottom Bar
    // ============================================================

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));

        JButton backBtn = new JButton("返回主菜单");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        backBtn.setForeground(ThemeColors.TEXT_MUTED);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        backBtn.addActionListener(e -> MainFrame.getInstance().showPanel("home"));

        bar.add(backBtn);
        return bar;
    }

    // ============================================================
    // Helpers
    // ============================================================

    private static String getWinRateAdvice(int rate) {
        if (rate >= 80) return "非常优秀！";
        if (rate >= 60) return "继续加油";
        if (rate >= 40) return "多多练习";
        return "新手起步";
    }
}
