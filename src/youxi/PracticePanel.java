package youxi;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;

import youxi.animation.AnimationScheduler;
import youxi.animation.Easing;
import youxi.animation.Interpolators;
import youxi.animation.ParticleEngine;
import youxi.dao.WrongQuestionDAO;
import youxi.model.Question;
import youxi.model.User;
import youxi.service.QuestionCache;
import youxi.theme.ThemeColors;
import youxi.util.SoundManager;
import youxi.ui.AnimatedBackgroundPanel;

public class PracticePanel extends AnimatedBackgroundPanel {

    private User user;
    private String category;
    private List<Question> questions;
    private int currentIndex = 0;
    private int correctCount = 0;

    private JLabel progressLabel;
    private JTextArea questionArea;
    private JLabel typeLabel;
    private JPanel[] optionPanels;
    private JLabel[] optionLabels;
    private boolean[] optionSelected;
    private JButton confirmBtn;
    private JButton nextBtn;
    private JLabel feedbackLabel;
    private JLabel explanationLabel;
    private JLabel statusLabel;

    private boolean answered;
    private JPanel feedbackPanel;

    private final WrongQuestionDAO wrongQuestionDAO = new WrongQuestionDAO();
    private final ParticleEngine particleEngine = new ParticleEngine();
    private javax.swing.Timer particleTimer;

    @Override
    public void addNotify() {
        super.addNotify();
        startParticleTimer();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (particleTimer != null) particleTimer.stop();
    }

    private void startParticleTimer() {
        if (particleTimer != null) particleTimer.stop();
        particleTimer = new javax.swing.Timer(16, e -> {
            particleEngine.update(16);
            if (particleEngine.particleCount() > 0) repaint();
        });
        particleTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        particleEngine.render((Graphics2D) g);
    }

    public PracticePanel() {
        super("picture_pro/Gemini_Generated_Image_eparpteparptepar.png",
              AnimatedBackgroundPanel.OrbTheme.EMERALD,
              new Color(0, 0, 0, 80));
        setLayout(new BorderLayout());
        setName("practice");

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

        add(createBottomBar(), BorderLayout.SOUTH);
    }

    public void startPractice(User user, String category) {
        this.user = user;
        this.category = category;
        this.correctCount = 0;
        this.currentIndex = 0;
        this.answered = false;

        statusLabel.setText("加载题目中...");
        new Thread(() -> {
            List<Question> qs = QuestionCache.getInstance().getByCategory(category);
            final List<Question> finalQs = qs;
            SwingUtilities.invokeLater(() -> {
                if (finalQs.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "该学科暂无题目。");
                    MainFrame.getInstance().showPanel("home");
                    return;
                }
                questions = finalQs;
                statusLabel.setText("共 " + questions.size() + " 题");
                loadQuestion(0);
            });
        }).start();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));

        progressLabel = new JLabel("训练模式");
        progressLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        progressLabel.setForeground(ThemeColors.TEXT_DARK);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        JLabel catLabel = new JLabel(category == null ? "" : category);
        catLabel.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        catLabel.setForeground(ThemeColors.TEXT_MUTED);

        JButton quitBtn = new JButton("退出");
        quitBtn.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        quitBtn.setContentAreaFilled(false);
        quitBtn.setBorderPainted(false);
        quitBtn.setFocusPainted(false);
        quitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        quitBtn.addActionListener(e -> MainFrame.getInstance().showPanel("home"));

        right.add(catLabel);
        right.add(quitBtn);

        bar.add(progressLabel, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel createCenterArea() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;

        typeLabel = new JLabel(" ");
        typeLabel.setFont(new Font("微软雅黑", Font.BOLD, 19));
        typeLabel.setForeground(ThemeColors.PRIMARY);
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 8, 0);
        center.add(typeLabel, gc);

        questionArea = new JTextArea();
        questionArea.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        questionArea.setForeground(ThemeColors.TEXT_DARK);
        questionArea.setOpaque(false);
        questionArea.setEditable(false);
        questionArea.setFocusable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 20, 0);
        center.add(questionArea, gc);

        optionPanels = new JPanel[4];
        optionLabels = new JLabel[4];
        optionSelected = new boolean[4];

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            JPanel opt = new JPanel();
            opt.setBackground(ThemeColors.OPTION_BG);
            opt.setOpaque(true);
            opt.setLayout(new BorderLayout());
            opt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.CARD_BORDER, 2),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
            opt.setPreferredSize(new Dimension(250, 64));
            opt.setMinimumSize(new Dimension(150, 58));
            opt.setCursor(new Cursor(Cursor.HAND_CURSOR));
            opt.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
            opt.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

            optionLabels[i] = new JLabel();
            optionLabels[i].setFont(new Font("微软雅黑", Font.PLAIN, 18));
            optionLabels[i].setForeground(ThemeColors.TEXT_DARK);
            opt.add(optionLabels[i], BorderLayout.CENTER);

            opt.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (!answered && !optionSelected[idx]) {
                        opt.setBackground(ThemeColors.OPTION_HOVER);
                        opt.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(ThemeColors.PRIMARY, 2),
                            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                    }
                }
                public void mouseExited(MouseEvent e) {
                    if (!answered && !optionSelected[idx]) {
                        opt.setBackground(ThemeColors.OPTION_BG);
                        opt.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(ThemeColors.CARD_BORDER, 2),
                            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                    }
                }
                public void mousePressed(MouseEvent e) {
                    if (!answered) opt.setBackground(ThemeColors.OPTION_SELECTED);
                }
                public void mouseReleased(MouseEvent e) {
                    if (answered) return;
                    if (opt.contains(e.getPoint())) {
                        Question q = questions.get(currentIndex);
                        if ("多选".equals(q.getType())) {
                            optionSelected[idx] = !optionSelected[idx];
                            opt.setBackground(optionSelected[idx] ? ThemeColors.OPTION_SELECTED : ThemeColors.OPTION_HOVER);
                        } else {
                            opt.setBackground(ThemeColors.OPTION_HOVER);
                            handleAnswer(idx);
                        }
                    } else {
                        opt.setBackground(optionSelected[idx] ? ThemeColors.OPTION_SELECTED : ThemeColors.OPTION_BG);
                    }
                }
            });

            optionPanels[i] = opt;
            gc.gridy = 2 + i;
            gc.insets = new Insets(0, 0, 8, 0);
            center.add(opt, gc);
        }

        confirmBtn = new JButton("确认提交");
        confirmBtn.setFont(new Font("微软雅黑", Font.BOLD, 19));
        confirmBtn.setForeground(ThemeColors.TEXT_WHITE);
        confirmBtn.setBackground(ThemeColors.PRIMARY);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.setPreferredSize(new Dimension(250, 52));
        confirmBtn.setVisible(false);
        confirmBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        confirmBtn.addActionListener(e -> {
            if (!answered) handleMultiSubmit();
        });
        gc.gridy = 6;
        gc.insets = new Insets(0, 0, 8, 0);
        center.add(confirmBtn, gc);

        feedbackPanel = new JPanel();
        feedbackPanel.setOpaque(false);
        feedbackPanel.setLayout(new BoxLayout(feedbackPanel, BoxLayout.Y_AXIS));
        feedbackPanel.setVisible(false);

        feedbackLabel = new JLabel();
        feedbackLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        explanationLabel = new JLabel();
        explanationLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        explanationLabel.setForeground(ThemeColors.TEXT_BODY);
        explanationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        feedbackPanel.add(feedbackLabel);
        feedbackPanel.add(Box.createVerticalStrut(6));
        feedbackPanel.add(explanationLabel);

        gc.gridy = 7;
        gc.insets = new Insets(8, 0, 8, 0);
        center.add(feedbackPanel, gc);

        nextBtn = new JButton("下一题 →");
        nextBtn.setFont(new Font("微软雅黑", Font.BOLD, 19));
        nextBtn.setForeground(ThemeColors.TEXT_WHITE);
        nextBtn.setBackground(ThemeColors.PRIMARY);
        nextBtn.setFocusPainted(false);
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextBtn.setPreferredSize(new Dimension(250, 52));
        nextBtn.setVisible(false);
        nextBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        nextBtn.addActionListener(e -> {
            if (currentIndex + 1 < questions.size()) {
                loadQuestion(currentIndex + 1);
            } else {
                finishPractice();
            }
        });
        gc.gridy = 8;
        gc.insets = new Insets(0, 0, 20, 0);
        center.add(nextBtn, gc);

        gc.gridy = 9;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        center.add(new JLabel(), gc);

        return center;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 24, 16, 24));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        statusLabel.setForeground(ThemeColors.SUCCESS);

        bar.add(statusLabel, BorderLayout.CENTER);
        return bar;
    }

    private void loadQuestion(int index) {
        if (questions == null || index >= questions.size()) {
            finishPractice();
            return;
        }
        currentIndex = index;
        answered = false;

        Question q = questions.get(index);

        boolean isMulti = "多选".equals(q.getType());
        boolean isJudge = "判断".equals(q.getType());
        String typeText = isMulti ? "[多选] 多选题（可多选后确认）" :
                          isJudge ? "[判断] 判断题（点击选项直接提交）" :
                          "[单选] 单选题（点击选项直接提交）";
        typeLabel.setText(typeText);

        questionArea.setText((index + 1) + ". " + q.getContent());
        questionArea.setCaretPosition(0);

        // subtle entrance: font size pop-in
        Font qBaseFont = questionArea.getFont();
        float qBaseSize = qBaseFont.getSize2D();
        questionArea.setFont(qBaseFont.deriveFont(qBaseSize * 0.7f));
        AnimationScheduler.getInstance().animate(250, Easing.EASE_OUT_CUBIC, t -> {
            float s = (float) Interpolators.lerpDouble(qBaseSize * 0.7f, qBaseSize, t);
            questionArea.setFont(qBaseFont.deriveFont(s));
        }, () -> questionArea.setFont(qBaseFont));

        String[] labels = {"A", "B", "C", "D"};
        String[] values = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};

        if (isJudge && (values[0] == null || values[0].isEmpty())
                && (values[1] == null || values[1].isEmpty())) {
            values[0] = "正确";
            values[1] = "错误";
        }

        int visibleCount = 0;
        for (int i = 0; i < 4; i++) {
            if (values[i] != null && !values[i].isEmpty()) {
                optionLabels[i].setText(labels[i] + ". " + values[i]);
                optionLabels[i].setForeground(ThemeColors.TEXT_DARK);
                optionPanels[i].setVisible(true);
                optionPanels[i].setBackground(ThemeColors.OPTION_BG);
                optionPanels[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeColors.CARD_BORDER, 2),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                optionPanels[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
                optionSelected[i] = false;
            } else {
                optionPanels[i].setVisible(false);
                optionSelected[i] = false;
            }
        }

        confirmBtn.setVisible(isMulti);
        feedbackPanel.setVisible(false);
        nextBtn.setVisible(false);

        progressLabel.setText("训练模式  " + (index + 1) + "/" + questions.size());
        statusLabel.setText("正确: " + correctCount);

        revalidate();
        repaint();
    }

    private void handleAnswer(int singleIdx) {
        if (answered) return;
        SoundManager.click();
        answered = true;
        Question q = questions.get(currentIndex);
        String userAns = String.valueOf((char) ('A' + singleIdx));
        processAnswer(q, userAns, singleIdx);
    }

    private void handleMultiSubmit() {
        if (answered) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (optionSelected[i] && optionPanels[i].isVisible()) {
                sb.append((char) ('A' + i));
            }
        }
        String userAns = sb.toString();
        if (userAns.isEmpty()) return;

        answered = true;
        Question q = questions.get(currentIndex);
        processAnswer(q, userAns, -1);
    }

    private void processAnswer(Question q, String userAns, int singleIdx) {
        String normalizedUser = userAns;
        // Sort answer for comparison
        char[] chars = userAns.toUpperCase().toCharArray();
        java.util.Arrays.sort(chars);
        String sortedUser = new String(chars);
        String correctAns = q.getAnswer();

        boolean correct = sortedUser.equalsIgnoreCase(correctAns);

        if (correct) {
            SoundManager.correct();
            correctCount++;
            feedbackLabel.setText("回答正确！");
            feedbackLabel.setForeground(ThemeColors.SUCCESS);

            if (singleIdx >= 0) {
                optionPanels[singleIdx].setBackground(ThemeColors.SUCCESS);
                optionLabels[singleIdx].setForeground(ThemeColors.TEXT_WHITE);
                optionPanels[singleIdx].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeColors.SUCCESS, 2),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                Point p = optionPanels[singleIdx].getLocationOnScreen();
                SwingUtilities.convertPointFromScreen(p, this);
                particleEngine.spawnCorrectBurst(p.x + optionPanels[singleIdx].getWidth() / 2.0,
                                                 p.y + optionPanels[singleIdx].getHeight() / 2.0);
            } else {
                for (int i = 0; i < 4; i++) {
                    if (optionSelected[i] && optionPanels[i].isVisible()) {
                        optionPanels[i].setBackground(ThemeColors.SUCCESS);
                        optionLabels[i].setForeground(ThemeColors.TEXT_WHITE);
                        optionPanels[i].setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(ThemeColors.SUCCESS, 2),
                            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                    }
                }
            }
        } else {
            SoundManager.wrong();
            feedbackLabel.setText("回答错误！正确答案: " + correctAns);
            feedbackLabel.setForeground(ThemeColors.DANGER);

            if (singleIdx >= 0) {
                optionPanels[singleIdx].setBackground(ThemeColors.DANGER);
                optionLabels[singleIdx].setForeground(ThemeColors.TEXT_WHITE);
                optionPanels[singleIdx].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeColors.DANGER, 2),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                shakePanel(optionPanels[singleIdx]);
                int correctIdx = correctAns.charAt(0) - 'A';
                optionPanels[correctIdx].setBackground(ThemeColors.SUCCESS);
                optionLabels[correctIdx].setForeground(ThemeColors.TEXT_WHITE);
                optionPanels[correctIdx].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeColors.SUCCESS, 2),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));
            } else {
                for (int i = 0; i < 4; i++) {
                    if (!optionPanels[i].isVisible()) continue;
                    boolean userPicked = optionSelected[i];
                    boolean isCorrect = correctAns.indexOf((char) ('A' + i)) >= 0;
                    if (userPicked && !isCorrect) {
                        optionPanels[i].setBackground(ThemeColors.DANGER);
                        optionLabels[i].setForeground(ThemeColors.TEXT_WHITE);
                        optionPanels[i].setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(ThemeColors.DANGER, 2),
                            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                        shakePanel(optionPanels[i]);
                    } else if (isCorrect) {
                        optionPanels[i].setBackground(ThemeColors.SUCCESS);
                        optionLabels[i].setForeground(ThemeColors.TEXT_WHITE);
                        optionPanels[i].setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(ThemeColors.SUCCESS, 2),
                            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                    }
                }
            }

            try {
                wrongQuestionDAO.insertOrIncrement(user.getId(), q.getId());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        String exp = q.getExplanation();
        if (exp != null && !exp.isEmpty()) {
            explanationLabel.setText("<html><body style='width:420px'> " + escapeHtml(exp) + "</body></html>");
        } else {
            explanationLabel.setText(" ");
        }

        statusLabel.setText("正确: " + correctCount);
        feedbackPanel.setVisible(true);
        nextBtn.setVisible(true);

        // Hide confirm button after answer
        confirmBtn.setVisible(false);

        // Disable hovering on options
        for (int i = 0; i < 4; i++) {
            optionPanels[i].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        revalidate();
        repaint();
    }

    private void shakePanel(JPanel panel) {
        final javax.swing.border.Border orig = panel.getBorder();
        javax.swing.Timer timer = new javax.swing.Timer(50, null);
        timer.addActionListener(new java.awt.event.ActionListener() {
            int tick = 0;
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (tick >= 6) {
                    panel.setBorder(orig);
                    ((javax.swing.Timer) e.getSource()).stop();
                    return;
                }
                boolean danger = (tick % 2 == 0);
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(danger ? ThemeColors.DANGER : ThemeColors.CARD_BORDER, 2),
                    BorderFactory.createEmptyBorder(11, 15, 11, 15)));
                tick++;
            }
        });
        timer.start();
    }

    private void finishPractice() {
        String msg = "练习结束！\n正确: " + correctCount + "/" + questions.size();
        JOptionPane.showMessageDialog(this, msg, "训练完成", JOptionPane.INFORMATION_MESSAGE);
        MainFrame.getInstance().showPanel("home");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
