package youxi;

import java.awt.*;
import java.sql.SQLException;

import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;

import youxi.animation.AnimationScheduler;
import youxi.animation.Easing;
import youxi.animation.Interpolators;
import youxi.controller.GameController;
import youxi.model.User;
import youxi.service.UserService;
import youxi.theme.ThemeColors;
import youxi.util.Config;
import youxi.ui.AnimatedBackgroundPanel;

public class LoginPanel extends AnimatedBackgroundPanel {

    private JLabel titleLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JLabel errorLabel;
    private final UserService userService = new UserService();

    public LoginPanel() {
        super("picture_pro/Gemini_Generated_Image_et46dqet46dqet46.png",
              AnimatedBackgroundPanel.OrbTheme.VIOLET,
              new Color(0, 0, 0, 100));
        setLayout(new GridBagLayout());

        add(createLoginCard(), new GridBagConstraints());

        SwingUtilities.invokeLater(this::tryAutoLogin);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        startTitleGlow();
    }

    private void startTitleGlow() {
        if (titleLabel == null) return;
        AnimationScheduler.getInstance().animate(2000, Easing.LINEAR, t -> {
            double glow = Math.sin(t * 2 * Math.PI) * 0.3 + 0.7;
            titleLabel.setForeground(Interpolators.lerpColor(ThemeColors.PRIMARY, new Color(180, 200, 255), glow));
        }, () -> startTitleGlow());
    }

    private void tryAutoLogin() {
        if ("true".equalsIgnoreCase(Config.get("login.skip_auto"))) {
            Config.set("login.skip_auto", "false");
            return;
        }
        String lastUser = Config.get("login.last_username");
        String expiryStr = Config.get("login.session_expiry");
        if (lastUser == null || lastUser.isEmpty() || expiryStr == null) return;

        try {
            long expiry = Long.parseLong(expiryStr);
            if (System.currentTimeMillis() > expiry) return;
        } catch (NumberFormatException e) { return; }

        setInputEnabled(false);
        loginBtn.setText("自动登录中...");
        new Thread(() -> {
            try {
                User user = userService.loginBySession(lastUser);
                if (user == null) {
                    SwingUtilities.invokeLater(() -> {
                        usernameField.setText(lastUser);
                        passwordField.requestFocus();
                        setInputEnabled(true);
                        loginBtn.setText("登 录");
                    });
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    Config.set("login.session_expiry", String.valueOf(System.currentTimeMillis() + 7L * 24 * 3600 * 1000));
                    GameController.getInstance().loginSucceeded(user);
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    setInputEnabled(true);
                    loginBtn.setText("登 录");
                });
            }
        }).start();
    }

    private JPanel createLoginCard() {
        JPanel card = new JPanel();
        card.setBackground(ThemeColors.CARD_BG);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));
        card.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel icon = new JLabel("◇", SwingConstants.CENTER);
        icon.setFont(new Font("微软雅黑", Font.PLAIN, 72));
        icon.setForeground(ThemeColors.PRIMARY);
        gc.insets = new Insets(0, 0, 12, 0);
        card.add(icon, gc);

        titleLabel = new JLabel("知识竞答", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(ThemeColors.PRIMARY);
        gc.insets = new Insets(0, 0, 4, 0);
        card.add(titleLabel, gc);

        JLabel subtitle = new JLabel("通信知识竞赛", SwingConstants.CENTER);
        subtitle.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        subtitle.setForeground(ThemeColors.TEXT_MUTED);
        gc.insets = new Insets(0, 0, 28, 0);
        card.add(subtitle, gc);

        usernameField = new JTextField();
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "用户名");
        usernameField.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        usernameField.setPreferredSize(new Dimension(200, 46));
        usernameField.setMinimumSize(new Dimension(150, 46));
        String lastUser = Config.get("login.last_username");
        if (lastUser != null && !lastUser.isEmpty()) {
            usernameField.setText(lastUser);
        }
        usernameField.addActionListener(e -> passwordField.requestFocus());
        gc.insets = new Insets(4, 0, 4, 0);
        card.add(usernameField, gc);

        passwordField = new JPasswordField();
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "密码");
        passwordField.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        passwordField.setPreferredSize(new Dimension(200, 46));
        passwordField.setMinimumSize(new Dimension(150, 46));
        passwordField.addActionListener(e -> doLogin());

        JButton eyeBtn = new JButton("●");
        eyeBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        eyeBtn.setFocusPainted(false);
        eyeBtn.setBorderPainted(false);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeBtn.setToolTipText("显示/隐藏密码");
        eyeBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == (char) 0) {
                passwordField.setEchoChar('•');
                eyeBtn.setText("●");
                eyeBtn.setForeground(ThemeColors.TEXT_MUTED);
            } else {
                passwordField.setEchoChar((char) 0);
                eyeBtn.setText("○");
                eyeBtn.setForeground(ThemeColors.PRIMARY);
            }
            passwordField.requestFocus();
        });
        passwordField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, eyeBtn);
        gc.insets = new Insets(4, 0, 4, 0);
        card.add(passwordField, gc);

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        errorLabel.setForeground(ThemeColors.DANGER);
        gc.insets = new Insets(4, 0, 4, 0);
        card.add(errorLabel, gc);

        loginBtn = new JButton("登 录");
        loginBtn.setFont(new Font("微软雅黑", Font.BOLD, 30));
        loginBtn.setForeground(ThemeColors.TEXT_WHITE);
        loginBtn.setBackground(ThemeColors.PRIMARY);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(200, 48));
        loginBtn.setMinimumSize(new Dimension(150, 48));
        loginBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        loginBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        loginBtn.addActionListener(e -> doLogin());
        gc.insets = new Insets(14, 0, 0, 0);
        card.add(loginBtn, gc);

        JButton registerBtn = new JButton("注册新账号");
        registerBtn.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        registerBtn.setForeground(ThemeColors.PRIMARY);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        registerBtn.addActionListener(e -> showRegisterDialog());
        gc.insets = new Insets(4, 0, 0, 0);
        card.add(registerBtn, gc);

        return card;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("请输入用户名和密码");
            return;
        }

        setInputEnabled(false);
        loginBtn.setText("登录中...");

        new Thread(() -> {
            try {
                User user = userService.login(username, password);
                SwingUtilities.invokeLater(() -> {
                    if (user != null) {
                        errorLabel.setText(" ");
                        Config.set("login.last_username", username);
                        Config.set("login.session_expiry", String.valueOf(System.currentTimeMillis() + 7L * 24 * 3600 * 1000));
                        GameController.getInstance().loginSucceeded(user);
                    } else {
                        showError("用户名或密码错误");
                    }
                    setInputEnabled(true);
                    loginBtn.setText("登 录");
                });
            } catch (SQLException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    showError("数据库连接失败，请稍后重试");
                    setInputEnabled(true);
                    loginBtn.setText("登 录");
                });
            }
        }).start();
    }

    private void showRegisterDialog() {
        JTextField regUser = new JTextField();
        regUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "输入用户名（至少3位）");
        regUser.setFont(new Font("微软雅黑", Font.PLAIN, 18));

        JPasswordField regPwd = new JPasswordField();
        regPwd.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "输入密码（至少6位）");
        regPwd.setFont(new Font("微软雅黑", Font.PLAIN, 18));

        JPasswordField regPwd2 = new JPasswordField();
        regPwd2.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "再次输入密码");
        regPwd2.setFont(new Font("微软雅黑", Font.PLAIN, 18));

        Object[] fields = {"用户名:", regUser, "密码:", regPwd, "确认密码:", regPwd2};
        int result = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                fields, "注册新账号", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String name = regUser.getText().trim();
        String pwd1 = new String(regPwd.getPassword());
        String pwd2 = new String(regPwd2.getPassword());

        if (name.length() < 3) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "用户名至少3位", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (pwd1.length() < 6) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "密码至少6位", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!pwd1.equals(pwd2)) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "两次密码不一致", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                User user = userService.register(name, pwd1);
                SwingUtilities.invokeLater(() -> {
                    if (user != null) {
                        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(LoginPanel.this),
                                "注册成功！\n欢迎，" + user.getUsername(), "提示", JOptionPane.INFORMATION_MESSAGE);
                        usernameField.setText(user.getUsername());
                        passwordField.setText("");
                        passwordField.requestFocus();
                    } else {
                        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(LoginPanel.this),
                                "注册失败，用户名可能已被占用", "提示", JOptionPane.WARNING_MESSAGE);
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(LoginPanel.this),
                            "数据库连接失败", "错误", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    private void setInputEnabled(boolean enabled) {
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        loginBtn.setEnabled(enabled);
    }
}
