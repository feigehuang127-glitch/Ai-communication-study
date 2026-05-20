package youxi;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;

import youxi.animation.*;
import youxi.dao.WrongQuestionDAO;
import youxi.service.QuestionCache;
import youxi.model.Question;
import youxi.model.User;
import youxi.model.WrongQuestion;

import youxi.theme.ThemeColors;
import youxi.ui.AnimatedBackgroundPanel;

public class WrongBookPanel extends AnimatedBackgroundPanel {

    private User user;
    private JPanel contentPanel;
    private JLabel statsLabel;
    private JLabel loadingLabel;
    private JScrollPane scrollPane;

    private final WrongQuestionDAO wrongQuestionDAO = new WrongQuestionDAO();

    public WrongBookPanel() {
        super("picture_pro/Gemini_Generated_Image_5qngql5qngql5qng.png",
              AnimatedBackgroundPanel.OrbTheme.CYAN,
              new Color(0, 0, 0, 80));
        setLayout(new BorderLayout());
        setName("wrongbook");

        add(createHeader(), BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(8, 24, 16, 24));

        loadingLabel = new JLabel("加载中...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        loadingLabel.setForeground(ThemeColors.TEXT_MUTED);
        loadingLabel.setVisible(false);

        ScrollablePanel centerWrapper = new ScrollablePanel();
        centerWrapper.setLayout(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(loadingLabel, BorderLayout.NORTH);
        centerWrapper.add(contentPanel, BorderLayout.CENTER);

        scrollPane = new JScrollPane(centerWrapper);
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

    /** Called when panel becomes visible — reload data */
    public void refreshData() {
        if (user == null) {
            showEmpty("请先登录");
            return;
        }
        contentPanel.removeAll();
        loadingLabel.setVisible(true);
        statsLabel.setText("加载中...");
        contentPanel.revalidate();
        contentPanel.repaint();

        new Thread(() -> {
            try {
                List<WrongQuestion> wqList = wrongQuestionDAO.findByUserId(user.getId());
                List<QuestionCardData> cardDataList = new ArrayList<>();
                for (WrongQuestion wq : wqList) {
                    Question q = QuestionCache.getInstance().getById(wq.getQuestionId());
                    if (q != null) {
                        cardDataList.add(new QuestionCardData(q, wq));
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    contentPanel.removeAll();
                    loadingLabel.setVisible(false);

                    if (cardDataList.isEmpty()) {
                        showEmpty("你还没有错题，很棒！");
                    } else {
                        statsLabel.setText("共 " + cardDataList.size() + " 道错题待复习");
                        for (int i = 0; i < cardDataList.size(); i++) {
                            contentPanel.add(createQuestionCard(cardDataList.get(i)));
                            if (i < cardDataList.size() - 1) {
                                contentPanel.add(Box.createVerticalStrut(10));
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
                    showEmpty("加载失败，请稍后重试");
                });
            }
        }).start();
    }

    private void showEmpty(String msg) {
        contentPanel.removeAll();
        statsLabel.setText("暂无错题");

        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("▣", SwingConstants.CENTER);
        icon.setFont(new Font("微软雅黑", Font.PLAIN, 72));
        icon.setForeground(ThemeColors.TEXT_MUTED);

        JLabel text = new JLabel(msg, SwingConstants.CENTER);
        text.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        text.setForeground(ThemeColors.TEXT_MUTED);

        emptyPanel.add(Box.createVerticalGlue());
        emptyPanel.add(icon);
        emptyPanel.add(Box.createVerticalStrut(12));
        emptyPanel.add(text);
        emptyPanel.add(Box.createVerticalGlue());

        contentPanel.add(emptyPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(24, 24, 8, 24));

        JLabel title = new JLabel("错题本");
        title.setFont(new Font("微软雅黑", Font.BOLD, 28));
        title.setForeground(ThemeColors.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsLabel = new JLabel(" ");
        statsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        statsLabel.setForeground(ThemeColors.TEXT_MUTED);
        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(statsLabel);

        return header;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 24, 20, 24));

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

    private JPanel createQuestionCard(QuestionCardData data) {
        Question q = data.question;
        WrongQuestion wq = data.wrongQuestion;

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(ThemeColors.CARD_BG);
        card.setOpaque(true);
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // Row 1: badges
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);

        String cat = q.getCategory() != null && !q.getCategory().isEmpty() ? q.getCategory() : "未分类";
        topRow.add(createBadge(cat, ThemeColors.TEAL));
        topRow.add(createBadge(q.getType(), getTypeColor(q.getType())));
        topRow.add(createBadge("难度 " + q.getDifficulty(), ThemeColors.WARNING));
        topRow.add(createBadge("错误 " + wq.getWrongCount() + " 次", ThemeColors.DANGER));

        gc.insets = new Insets(0, 0, 10, 0);
        card.add(topRow, gc);

        // Row 2: question content
        JTextArea contentArea = createWrappingArea(q.getContent());
        contentArea.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        contentArea.setForeground(ThemeColors.TEXT_DARK);
        gc.insets = new Insets(0, 0, 8, 0);
        card.add(contentArea, gc);

        // Row 3: options
        String[] labels = {"A", "B", "C", "D"};
        String[] values = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
        for (int i = 0; i < 4; i++) {
            if (values[i] != null && !values[i].isEmpty()) {
                JTextArea opt = createWrappingArea(labels[i] + ". " + values[i]);
                opt.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                opt.setForeground(ThemeColors.TEXT_BODY);
                gc.insets = new Insets(1, 0, 1, 0);
                card.add(opt, gc);
            }
        }

        // Row 4: correct answer
        gc.insets = new Insets(8, 0, 0, 0);
        JLabel answerLabel = new JLabel("正确答案: " + q.getAnswer());
        answerLabel.setFont(new Font("微软雅黑", Font.BOLD, 17));
        answerLabel.setForeground(ThemeColors.SUCCESS);
        card.add(answerLabel, gc);

        // Row 5: explanation (if present)
        if (q.getExplanation() != null && !q.getExplanation().isEmpty()) {
            gc.insets = new Insets(6, 0, 0, 0);
            JTextArea expArea = createWrappingArea("" + q.getExplanation());
            expArea.setFont(new Font("微软雅黑", Font.PLAIN, 19));
            expArea.setForeground(ThemeColors.TEXT_MUTED);
            card.add(expArea, gc);
        }

        // Row 6: action buttons
        gc.insets = new Insets(12, 0, 0, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        JButton removeBtn = new JButton("移出错题本");
        removeBtn.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        removeBtn.setForeground(ThemeColors.DANGER);
        removeBtn.setContentAreaFilled(false);
        removeBtn.setBorderPainted(false);
        removeBtn.setFocusPainted(false);
        removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeBtn.addActionListener(e -> removeWrongQuestion(wq, card));
        btnRow.add(removeBtn);

        card.add(btnRow, gc);

        return card;
    }

    private JPanel createBadge(String text, Color color) {
        JPanel badge = new JPanel();
        badge.setOpaque(true);
        badge.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        badge.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        badge.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        label.setForeground(color);
        badge.add(label);

        return badge;
    }

    private Color getTypeColor(String type) {
        if ("多选".equals(type)) return ThemeColors.WARNING;
        if ("判断".equals(type)) return ThemeColors.PURPLE;
        return ThemeColors.PRIMARY;
    }

    private void removeWrongQuestion(WrongQuestion wq, JPanel card) {
        new Thread(() -> {
            try {
                wrongQuestionDAO.delete(user.getId(), wq.getQuestionId());
                SwingUtilities.invokeLater(() -> {
                    contentPanel.remove(card);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                    // Update stats
                    int remaining = 0;
                    for (Component c : contentPanel.getComponents()) {
                        if (c instanceof JPanel && c != card) remaining++;
                    }
                    if (remaining == 0 || (remaining == 1
                            && contentPanel.getComponent(0) instanceof JPanel
                            && ((JPanel) contentPanel.getComponent(0)).getComponentCount() > 0
                            && ((JPanel) contentPanel.getComponent(0)).getComponent(0) instanceof JLabel
                            && "▣".equals(((JLabel) ((JPanel) contentPanel.getComponent(0))
                                    .getComponent(0)).getText()))) {
                        showEmpty("你还没有错题，很棒！");
                    } else {
                        statsLabel.setText("共 " + remaining + " 道错题待复习");
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this, "移除失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private JTextArea createWrappingArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setColumns(40);
        return area;
    }

    /** JPanel that tracks viewport width for proper CJK text wrapping */
    private static class ScrollablePanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 100;
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Internal data class to hold question + wrong record together */
    private static class QuestionCardData {
        final Question question;
        final WrongQuestion wrongQuestion;

        QuestionCardData(Question question, WrongQuestion wrongQuestion) {
            this.question = question;
            this.wrongQuestion = wrongQuestion;
        }
    }
}
