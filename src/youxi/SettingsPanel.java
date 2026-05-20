package youxi;

import java.awt.*;
import java.sql.SQLException;

import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;

import youxi.dao.UserDAO;
import youxi.model.User;
import youxi.theme.ThemeColors;
import youxi.theme.ThemeManager;
import youxi.util.BCryptUtil;
import youxi.util.SoundManager;
import youxi.ui.AnimatedBackgroundPanel;

public class SettingsPanel extends AnimatedBackgroundPanel {

    private User user;
    private JLabel usernameLabel;
    private JLabel rankLabel;
    private JLabel scoreLabel;
    private JPasswordField currentPwField;
    private JPasswordField newPwField;
    private JPasswordField confirmPwField;
    private JLabel statusLabel;

    private final UserDAO userDAO = new UserDAO();

    public SettingsPanel() {
        super("picture_pro/Gemini_Generated_Image_hkshtkhkshtkhksh.png",
              AnimatedBackgroundPanel.OrbTheme.SLATE,
              new Color(0, 0, 0, 75));
        setLayout(new BorderLayout());
        setName("settings");

        add(createTopBar(), BorderLayout.NORTH);

        JPanel centerArea = createCenterArea();
        JScrollPane scrollPane = new JScrollPane(centerArea);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = scrollPane.getViewport().getWidth();
                if (w > 0) centerArea.setPreferredSize(new Dimension(w, centerArea.getPreferredSize().height));
                centerArea.revalidate();
            }
        });
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            usernameLabel.setText(user.getUsername());
            rankLabel.setText("段位: " + user.getRank());
            scoreLabel.setText("积分: " + user.getTotalScore());
        }
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));

        JLabel title = new JLabel("设置");
        title.setFont(new Font("微软雅黑", Font.BOLD, 20));
        title.setForeground(ThemeColors.TEXT_DARK);

        JButton backBtn = new JButton("返回");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            clearFields();
            MainFrame.getInstance().showPanel("home");
        });

        bar.add(title, BorderLayout.WEST);
        bar.add(backBtn, BorderLayout.EAST);
        return bar;
    }

    private JPanel createCenterArea() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(8, 24, 16, 24));

        // --- User info card ---
        JPanel infoCard = new JPanel();
        infoCard.setBackground(ThemeColors.OPTION_BG);
        infoCard.setOpaque(true);
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        infoCard.setPreferredSize(new Dimension(250, 180));
        infoCard.setMaximumSize(new Dimension(Short.MAX_VALUE, 180));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCard.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        JLabel infoTitle = new JLabel("个人信息");
        infoTitle.setFont(new Font("微软雅黑", Font.BOLD, 30));
        infoTitle.setForeground(ThemeColors.TEXT_DARK);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameLabel = new JLabel();
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        usernameLabel.setForeground(ThemeColors.TEXT_BODY);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        rankLabel = new JLabel();
        rankLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        rankLabel.setForeground(ThemeColors.TEXT_BODY);
        rankLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        scoreLabel = new JLabel();
        scoreLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        scoreLabel.setForeground(ThemeColors.TEXT_BODY);
        scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoCard.add(infoTitle);
        infoCard.add(Box.createVerticalStrut(12));
        infoCard.add(usernameLabel);
        infoCard.add(Box.createVerticalStrut(6));
        infoCard.add(rankLabel);
        infoCard.add(Box.createVerticalStrut(6));
        infoCard.add(scoreLabel);

        // --- Password card ---
        JPanel pwCard = new JPanel();
        pwCard.setBackground(ThemeColors.OPTION_BG);
        pwCard.setOpaque(true);
        pwCard.setLayout(new BoxLayout(pwCard, BoxLayout.Y_AXIS));
        pwCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        pwCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwCard.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        JLabel pwTitle = new JLabel("修改密码");
        pwTitle.setFont(new Font("微软雅黑", Font.BOLD, 30));
        pwTitle.setForeground(ThemeColors.TEXT_DARK);
        pwTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        currentPwField = createPwField("当前密码");
        newPwField = createPwField("新密码（至少6位）");
        confirmPwField = createPwField("确认新密码");

        JButton saveBtn = new JButton("保存密码");
        saveBtn.setFont(new Font("微软雅黑", Font.BOLD, 30));
        saveBtn.setForeground(ThemeColors.TEXT_WHITE);
        saveBtn.setBackground(ThemeColors.PRIMARY);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setPreferredSize(new Dimension(200, 40));
        saveBtn.setMaximumSize(new Dimension(200, 40));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        saveBtn.addActionListener(e -> handleChangePassword());

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        pwCard.add(pwTitle);
        pwCard.add(Box.createVerticalStrut(16));
        pwCard.add(currentPwField);
        pwCard.add(Box.createVerticalStrut(10));
        pwCard.add(newPwField);
        pwCard.add(Box.createVerticalStrut(10));
        pwCard.add(confirmPwField);
        pwCard.add(Box.createVerticalStrut(16));
        pwCard.add(saveBtn);
        pwCard.add(Box.createVerticalStrut(8));
        pwCard.add(statusLabel);

        center.add(infoCard);
        center.add(Box.createVerticalStrut(16));

        // --- Theme + Sound card ---
        JPanel themeCard = new JPanel();
        themeCard.setBackground(ThemeColors.CARD_BG);
        themeCard.setOpaque(true);
        themeCard.setLayout(new BoxLayout(themeCard, BoxLayout.Y_AXIS));
        themeCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        themeCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        themeCard.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        JLabel themeTitle = new JLabel("外观与音效");
        themeTitle.setFont(new Font("微软雅黑", Font.BOLD, 20));
        themeTitle.setForeground(ThemeColors.TEXT_DARK);
        themeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Theme toggle row
        JPanel themeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        themeRow.setOpaque(false);
        themeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel themeLabel = new JLabel("主题：");
        themeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        themeLabel.setForeground(ThemeColors.TEXT_BODY);

        JButton lightBtn = new JButton("[浅] 浅色");
        lightBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        JButton darkBtn = new JButton("[暗] 暗色");
        darkBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        Runnable updateThemeBtns = () -> {
            boolean isDark = ThemeManager.isDark();
            lightBtn.setBackground(isDark ? null : ThemeColors.PRIMARY);
            lightBtn.setForeground(isDark ? ThemeColors.TEXT_BODY : ThemeColors.TEXT_WHITE);
            darkBtn.setBackground(isDark ? ThemeColors.PRIMARY : null);
            darkBtn.setForeground(isDark ? ThemeColors.TEXT_WHITE : ThemeColors.TEXT_BODY);
        };

        lightBtn.setContentAreaFilled(false);
        lightBtn.setOpaque(true);
        lightBtn.setFocusPainted(false);
        lightBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lightBtn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        lightBtn.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        lightBtn.addActionListener(e -> {
            if (ThemeManager.isDark()) { ThemeManager.toggle(); updateThemeBtns.run(); }
        });

        darkBtn.setContentAreaFilled(false);
        darkBtn.setOpaque(true);
        darkBtn.setFocusPainted(false);
        darkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        darkBtn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        darkBtn.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        darkBtn.addActionListener(e -> {
            if (!ThemeManager.isDark()) { ThemeManager.toggle(); updateThemeBtns.run(); }
        });

        themeRow.add(themeLabel);
        themeRow.add(lightBtn);
        themeRow.add(darkBtn);

        // Difficulty preference row
        JPanel diffRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        diffRow.setOpaque(false);
        diffRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel diffLabel = new JLabel("难度：");
        diffLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        diffLabel.setForeground(ThemeColors.TEXT_BODY);

        String[] diffOptions = {"偏易", "默认", "偏难"};
        JComboBox<String> diffCombo = new JComboBox<>(diffOptions);
        diffCombo.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        diffCombo.setFocusable(false);
        String savedDiff = youxi.util.Config.get("game.difficulty_preference", "默认");
        diffCombo.setSelectedItem(savedDiff);
        diffCombo.addActionListener(e ->
            youxi.util.Config.set("game.difficulty_preference", (String) diffCombo.getSelectedItem()));

        diffRow.add(diffLabel);
        diffRow.add(diffCombo);

        // Sound toggle row
        JPanel soundRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        soundRow.setOpaque(false);
        soundRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel soundLabel = new JLabel("音效：");
        soundLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        soundLabel.setForeground(ThemeColors.TEXT_BODY);

        JCheckBox soundCheck = new JCheckBox("开启音效");
        soundCheck.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        soundCheck.setOpaque(false);
        soundCheck.setForeground(ThemeColors.TEXT_BODY);
        soundCheck.setSelected(!SoundManager.isMuted());
        soundCheck.addActionListener(e -> SoundManager.setMuted(!soundCheck.isSelected()));

        soundRow.add(soundLabel);
        soundRow.add(soundCheck);

        themeCard.add(themeTitle);
        themeCard.add(Box.createVerticalStrut(14));
        themeCard.add(themeRow);
        themeCard.add(Box.createVerticalStrut(8));
        themeCard.add(diffRow);
        themeCard.add(Box.createVerticalStrut(8));
        themeCard.add(soundRow);

        SwingUtilities.invokeLater(updateThemeBtns::run);

        center.add(themeCard);
        center.add(Box.createVerticalStrut(16));
        center.add(pwCard);

        return center;
    }

    private JPasswordField createPwField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        field.setPreferredSize(new Dimension(200, 40));
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        return field;
    }

    private void handleChangePassword() {
        if (user == null) return;

        String currentPw = new String(currentPwField.getPassword());
        String newPw = new String(newPwField.getPassword());
        String confirmPw = new String(confirmPwField.getPassword());

        if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            statusLabel.setForeground(ThemeColors.DANGER);
            statusLabel.setText("请填写所有密码字段");
            return;
        }

        if (!BCryptUtil.check(currentPw, user.getPasswordHash())) {
            statusLabel.setForeground(ThemeColors.DANGER);
            statusLabel.setText("当前密码错误");
            return;
        }

        if (newPw.length() < 6) {
            statusLabel.setForeground(ThemeColors.DANGER);
            statusLabel.setText("新密码至少6位");
            return;
        }

        if (!newPw.equals(confirmPw)) {
            statusLabel.setForeground(ThemeColors.DANGER);
            statusLabel.setText("两次输入的新密码不一致");
            return;
        }

        new Thread(() -> {
            try {
                String newHash = BCryptUtil.hash(newPw);
                userDAO.updatePassword(user.getId(), newHash);
                user.setPasswordHash(newHash);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setForeground(ThemeColors.SUCCESS);
                    statusLabel.setText("密码修改成功！");
                    clearFields();
                });
            } catch (SQLException ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setForeground(ThemeColors.DANGER);
                    statusLabel.setText("保存失败，请稍后重试");
                });
            }
        }).start();
    }

    private void clearFields() {
        currentPwField.setText("");
        newPwField.setText("");
        confirmPwField.setText("");
        statusLabel.setText(" ");
    }
}
