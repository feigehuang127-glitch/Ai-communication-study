package youxi;

import java.awt.*;

import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;

import youxi.controller.GameController;
import youxi.model.User;
import youxi.service.UserService;
import youxi.theme.ThemeColors;
import youxi.ui.AnimatedBackgroundPanel;
import youxi.ui.HoverCard;

public class CategorySelectPanel extends AnimatedBackgroundPanel {

    private User user;
    private boolean practiceMode;
    private JLabel rankLabel;
    private JLabel scoreLabel;

    public CategorySelectPanel() {
        super("picture_pro/Gemini_Generated_Image_m0qrvem0qrvem0qr.png",
              AnimatedBackgroundPanel.OrbTheme.CYAN,
              new Color(0, 0, 0, 70));
        setLayout(new BorderLayout());

        JPanel card = new JPanel();
        card.setBackground(ThemeColors.CARD_BG);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));
        card.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("选择题库", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 28));
        title.setForeground(ThemeColors.TEXT_DARK);
        gc.insets = new Insets(0, 0, 4, 0);
        card.add(title, gc);

        rankLabel = new JLabel("", SwingConstants.CENTER);
        rankLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        rankLabel.setForeground(ThemeColors.TEXT_MUTED);
        gc.insets = new Insets(0, 0, 24, 0);
        card.add(rankLabel, gc);

        String[][] subjects = {
            {"通信原理",     "调制解调、信道编码、香农定理…"},
            {"数据通信网",   "TCP/IP、路由协议、网络安全…"},
            {"光纤传输",     "光纤特性、SDH/MSTP、光器件…"},
            {"宽带接入技术", "ADSL、PON、FTTx、接入网…"},
            {"现代交换技术", "电路交换、分组交换、软交换…"},
            {"信息通信新技术", "5G、物联网、云计算、大数据…"}
        };
        Color[][] colors = ThemeColors.SUBJECT_COLORS;

        for (int i = 0; i < subjects.length; i++) {
            JPanel btn = createSubjectCard(subjects[i][0], subjects[i][1], colors[i][0], colors[i][1]);
            gc.insets = new Insets(6, 0, 6, 0);
            card.add(btn, gc);
        }

        JButton backBtn = new JButton("返回主菜单");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        backBtn.setForeground(ThemeColors.TEXT_MUTED);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        backBtn.addActionListener(e -> MainFrame.getInstance().showPanel("home"));
        gc.insets = new Insets(20, 0, 0, 0);
        card.add(backBtn, gc);

        JScrollPane scrollPane = new JScrollPane(card);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = scrollPane.getViewport().getWidth();
                if (w > 0) card.setPreferredSize(new Dimension(w, card.getPreferredSize().height));
                card.revalidate();
            }
        });
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setUser(User user) {
        this.user = user;
        this.practiceMode = false;
        if (user != null) {
            rankLabel.setText("段位: " + user.getRank() + "  |  积分: " + user.getTotalScore());
        }
    }

    public void setPracticeMode(User user) {
        this.user = user;
        this.practiceMode = true;
        if (user != null) {
            rankLabel.setText("段位: " + user.getRank() + "  |  积分: " + user.getTotalScore());
        }
    }

    private JPanel createSubjectCard(String title, String desc, Color color, Color colorDark) {
        HoverCard card = new HoverCard(colorDark, color);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        card.setPreferredSize(new Dimension(420, 112));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 112));
        card.setOnClick(() -> {
            String subject = title.replaceAll("[^\\u4e00-\\u9fff]", "");
            startGame(subject);
        });

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        titleLabel.setForeground(ThemeColors.TEXT_WHITE);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        descLabel.setForeground(new Color(255, 255, 255, 180));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(titleLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(descLabel);

        card.add(left, BorderLayout.CENTER);
        return card;
    }

    private void startGame(String category) {
        if (user == null) return;
        GameController gc = GameController.getInstance();
        if (practiceMode) gc.goToPractice(category);
        else gc.goToGame(category);
    }
}
