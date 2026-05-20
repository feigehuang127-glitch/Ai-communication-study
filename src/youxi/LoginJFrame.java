package youxi;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginJFrame extends JFrame {
    public static void main(String[] args) {
        // 1. 设置为系统原生风格 (Windows下就是Win10/11风格，Mac下就是Mac风格)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new LoginJFrame());
    }

    private JTextField usernameInput;
    private JPasswordField passwordInput;

    public LoginJFrame() {
        this.setSize(400, 350); // 调整了窗口大小，让比例更协调
        this.setTitle("知识竞答游戏 - 登录");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(null);

        // 2. 将背景设置为纯白，显得更干净现代
        this.getContentPane().setBackground(Color.WHITE);

        initView();
        this.setVisible(true);
    }

    private void initView() {
        // 定义全局字体 (微软雅黑, 平滑且现代)
        Font labelFont = new Font("微软雅黑", Font.BOLD, 30);
        Font inputFont = new Font("微软雅黑", Font.PLAIN, 18);

        // 标题 Label (额外加的一个大标题)
        JLabel titleLabel = new JLabel("系 统 登 录", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        titleLabel.setForeground(new Color(51, 51, 51)); // 深灰色
        titleLabel.setBounds(0, 30, 400, 40);
        this.add(titleLabel);

        // 用户名标签
        JLabel userLabel = new JLabel("用户名:");
        userLabel.setFont(labelFont);
        userLabel.setBounds(70, 100, 60, 35);
        this.add(userLabel);

        // 用户名输入框
        usernameInput = new JTextField();
        usernameInput.setFont(inputFont);
        usernameInput.setBounds(140, 100, 180, 35);
        // 稍微美化一下输入框边缘
        usernameInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 10, 0, 10) // 增加内边距
        ));
        this.add(usernameInput);

        // 密码标签
        JLabel pwdLabel = new JLabel("密  码:");
        pwdLabel.setFont(labelFont);
        pwdLabel.setBounds(70, 160, 60, 35);
        this.add(pwdLabel);

        // 密码输入框
        passwordInput = new JPasswordField();
        passwordInput.setFont(inputFont);
        passwordInput.setBounds(140, 160, 180, 35);
        passwordInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        this.add(passwordInput);

        // 登录按钮美化
        JButton loginBtn = new JButton("登 录");
        loginBtn.setFont(new Font("微软雅黑", Font.BOLD, 20));
        loginBtn.setBackground(new Color(0, 120, 215)); // 类似Win10的蓝色
        loginBtn.setForeground(Color.WHITE); // 按钮文字设为白色
        loginBtn.setFocusPainted(false); // 去除点击时的虚线框
        loginBtn.setBorderPainted(false); // 去除原生边框，使用纯色
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 鼠标放上去变小手
        loginBtn.setBounds(140, 230, 180, 40); // 按钮加宽
        this.add(loginBtn);

        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkLogin();
            }
        });
    }

    private void checkLogin() {
        // ... (保持你原有的数据库验证逻辑不变) ...
        String username = usernameInput.getText();
        String password = new String(passwordInput.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名或密码不能为空！");
            return;
        }

        String url = "jdbc:mysql://127.0.0.1:3306/game_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String dbUser = "root";
        String dbPwd = "123456";

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPwd);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "登录成功，欢迎进入游戏！");
                    this.setVisible(false);
                    // new GameJFrame();
                } else {
                    JOptionPane.showMessageDialog(this, "账号或密码错误，请重试！");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接失败！\n" + e.getMessage());
        }
    }
}