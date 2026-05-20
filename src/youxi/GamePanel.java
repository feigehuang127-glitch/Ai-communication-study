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
import youxi.controller.GameController;
import youxi.dao.AchievementDAO;
import youxi.dao.DailyChallengeDAO;
import youxi.dao.GameHistoryDAO;
import youxi.dao.WrongQuestionDAO;
import youxi.model.Badge;
import youxi.model.GameHistory;
import youxi.model.Question;
import youxi.model.User;
import youxi.service.GameService;
import youxi.service.UserService;
import youxi.theme.ThemeColors;
import youxi.util.Config;
import youxi.util.SoundManager;
import youxi.ui.AnimatedBackgroundPanel;

public class GamePanel extends AnimatedBackgroundPanel {

    private User user;
    private String category;
    private List<Question> questions;
    private int currentIndex = 0;
    private int correctCount = 0;
    private int comboCount = 0;
    private int speedBonus = 0;
    private int comboBonus = 0;
    private boolean comboAchieved = false;
    private long gameStartTimeMs;

    private int secondsLeft;
    private javax.swing.Timer gameTimer;

    private JLabel progressLabel;
    private JProgressBar progressBar;
    private JTextArea questionArea;
    private JLabel typeLabel;
    private JPanel[] optionPanels;
    private JLabel[] optionLabels;
    private boolean[] optionSelected;
    private JButton confirmBtn;
    private JPanel feedbackPanel;
    private JLabel gameFeedbackLabel;
    private JLabel gameExplanationLabel;
    private JLabel statusLabel;

    private volatile boolean locked;
    private long questionStartTime;

    private final GameService gameService = new GameService();
    private final GameHistoryDAO gameHistoryDAO = new GameHistoryDAO();
    private final WrongQuestionDAO wrongQuestionDAO = new WrongQuestionDAO();
    private final UserService userService = new UserService();
    private final AchievementDAO achievementDAO = new AchievementDAO();
    private final DailyChallengeDAO dailyChallengeDAO = new DailyChallengeDAO();
    private final ParticleEngine particleEngine = new ParticleEngine();

    private boolean dailyChallenge;
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
            if (particleEngine.particleCount() > 0) {
                repaint();
            }
        });
        particleTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        particleEngine.render(g2);
    }

    public GamePanel() {
        super("picture_pro/Gemini_Generated_Image_shxn6kshxn6kshxn.png",
              AnimatedBackgroundPanel.OrbTheme.VIOLET,
              new Color(0, 0, 0, 85));
        setLayout(new BorderLayout());
        setName("game");

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

        // 键盘快捷键
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        String[] keys = {"1", "2", "3", "4"};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            im.put(KeyStroke.getKeyStroke(keys[i]), "select_" + i);
            am.put("select_" + i, new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (locked || questions == null || optionPanels[idx] == null) return;
                    if (!optionPanels[idx].isVisible()) return;
                    Question q = questions.get(currentIndex);
                    if ("多选".equals(q.getType())) {
                        optionSelected[idx] = !optionSelected[idx];
                        optionPanels[idx].setBackground(optionSelected[idx] ? ThemeColors.OPTION_SELECTED : ThemeColors.OPTION_BG);
                    } else {
                        handleOptionClick(idx);
                    }
                }
            });
        }
        im.put(KeyStroke.getKeyStroke("ENTER"), "confirm");
        am.put("confirm", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (locked || questions == null) return;
                Question q = questions.get(currentIndex);
                if ("多选".equals(q.getType())) {
                    handleMultiSubmit();
                } else {
                    // 单选时 Enter 选择当前高亮的选项，或什么都不做
                    for (int i = 0; i < 4; i++) {
                        if (optionSelected[i]) {
                            handleOptionClick(i);
                            return;
                        }
                    }
                }
            }
        });
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "back");
        am.put("back", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (gameTimer != null) gameTimer.stop();
                GameController.getInstance().goToCategory(false);
            }
        });
    }

    public void startGame(User user, String category) {
        this.user = user;
        this.category = category;
        this.correctCount = 0;
        this.comboCount = 0;
        this.speedBonus = 0;
        this.comboBonus = 0;
        this.comboAchieved = false;
        this.gameStartTimeMs = System.currentTimeMillis();
        this.currentIndex = 0;
        this.locked = false;

        statusLabel.setText("加载题目中...");
        new Thread(() -> {
            float accuracy = 0.5f;
            try { accuracy = gameHistoryDAO.getRecentAccuracy(user.getId(), 30); }
            catch (SQLException ignored) {}
            List<Question> qs = gameService.generateQuestions(user, category, accuracy);
            SwingUtilities.invokeLater(() -> {
                if (qs.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "该学科暂无题目，请先导入题库。");
                    GameController.getInstance().goToHome();
                    return;
                }
                questions = qs;
                statusLabel.setText("共 " + questions.size() + " 题");
                loadQuestion(0);
            });
        }).start();
    }

    public void startDailyChallenge(User user) {
        this.user = user;
        this.category = "每日挑战";
        this.dailyChallenge = true;
        this.correctCount = 0;
        this.comboCount = 0;
        this.speedBonus = 0;
        this.comboBonus = 0;
        this.comboAchieved = false;
        this.gameStartTimeMs = System.currentTimeMillis();
        this.currentIndex = 0;
        this.locked = false;

        statusLabel.setText("加载每日挑战...");
        new Thread(() -> {
            List<Question> qs = gameService.generateDailyQuestions(user);
            SwingUtilities.invokeLater(() -> {
                if (qs.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "暂无题目，请先导入题库。");
                    GameController.getInstance().goToHome();
                    return;
                }
                questions = qs;
                statusLabel.setText("每日挑战 · 共 " + questions.size() + " 题 · 积分×2");
                loadQuestion(0);
            });
        }).start();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));

        progressLabel = new JLabel("准备");
        progressLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        progressLabel.setForeground(ThemeColors.TEXT_DARK);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(100);
        progressBar.setPreferredSize(new Dimension(150, 10));
        progressBar.putClientProperty(FlatClientProperties.STYLE, "arc: 4");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        JButton quitBtn = new JButton("退出");
        quitBtn.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        quitBtn.setContentAreaFilled(false);
        quitBtn.setBorderPainted(false);
        quitBtn.setFocusPainted(false);
        quitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        quitBtn.addActionListener(e -> {
            if (gameTimer != null) gameTimer.stop();
            MainFrame.getInstance().showPanel("home");
        });

        JLabel catLabel = new JLabel(category == null ? "" : category);
        catLabel.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        catLabel.setForeground(ThemeColors.TEXT_MUTED);

        right.add(catLabel);
        right.add(quitBtn);

        bar.add(progressLabel, BorderLayout.WEST);
        bar.add(progressBar, BorderLayout.CENTER);
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
                    if (!optionSelected[idx]) {
                        opt.setBackground(ThemeColors.OPTION_HOVER);
                        opt.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(ThemeColors.PRIMARY, 2),
                            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                    }
                }
                public void mouseExited(MouseEvent e) {
                    if (!optionSelected[idx]) {
                        opt.setBackground(ThemeColors.OPTION_BG);
                        opt.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(ThemeColors.CARD_BORDER, 2),
                            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                    }
                }
                public void mousePressed(MouseEvent e) {
                    opt.setBackground(ThemeColors.OPTION_SELECTED);
                }
                public void mouseReleased(MouseEvent e) {
                    if (opt.contains(e.getPoint()) && !locked) {
                        opt.setBackground(optionSelected[idx] ? ThemeColors.OPTION_SELECTED : ThemeColors.OPTION_HOVER);
                        handleOptionClick(idx);
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
        confirmBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        confirmBtn.addActionListener(e -> {
            if (!locked) handleMultiSubmit();
        });
        gc.gridy = 6;
        gc.insets = new Insets(0, 0, 8, 0);
        center.add(confirmBtn, gc);

        feedbackPanel = new JPanel();
        feedbackPanel.setOpaque(false);
        feedbackPanel.setLayout(new BoxLayout(feedbackPanel, BoxLayout.Y_AXIS));
        feedbackPanel.setVisible(false);

        gameFeedbackLabel = new JLabel();
        gameFeedbackLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        gameFeedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        gameExplanationLabel = new JLabel();
        gameExplanationLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        gameExplanationLabel.setForeground(ThemeColors.TEXT_BODY);
        gameExplanationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        feedbackPanel.add(gameFeedbackLabel);
        feedbackPanel.add(Box.createVerticalStrut(4));
        feedbackPanel.add(gameExplanationLabel);

        gc.gridy = 7;
        gc.insets = new Insets(8, 0, 0, 0);
        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        center.add(feedbackPanel, gc);

        gc.gridy = 8;
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
            endGame();
            return;
        }
        currentIndex = index;
        locked = false;
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

        // 判断题且选项为空时，默认填入"正确"/"错误"
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
                optionSelected[i] = false;
                optionPanels[i].setBackground(ThemeColors.OPTION_BG);
                optionPanels[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeColors.CARD_BORDER, 2),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                optionPanels[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
                visibleCount++;
            } else {
                optionPanels[i].setVisible(false);
                optionSelected[i] = false;
            }
        }

        confirmBtn.setVisible(isMulti);
        feedbackPanel.setVisible(false);

        revalidate();
        repaint();

        progressLabel.setText((index + 1) + "/" + questions.size());
        statusLabel.setText("正确: " + correctCount + "  |  连对: " + comboCount);

        startTimer();
    }

    private void startTimer() {
        if (gameTimer != null) gameTimer.stop();
        secondsLeft = Config.timePerQuestion();
        updateProgressBar();
        questionStartTime = System.currentTimeMillis();

        gameTimer = new javax.swing.Timer(1000, e -> {
            secondsLeft--;
            updateProgressBar();
            if (secondsLeft <= 0) {
                gameTimer.stop();
                handleTimeout();
            }
        });
        gameTimer.start();
    }

    private void updateProgressBar() {
        int val = secondsLeft * 10;
        progressBar.setValue(val);
        progressLabel.setText(secondsLeft + "s");

        if (secondsLeft > 5) progressBar.setForeground(ThemeColors.SUCCESS);
        else if (secondsLeft > 3) progressBar.setForeground(ThemeColors.GOLD);
        else progressBar.setForeground(ThemeColors.DANGER);
    }

    private void handleOptionClick(int idx) {
        SoundManager.click();
        Question q = questions.get(currentIndex);
        if ("多选".equals(q.getType())) {
            optionSelected[idx] = !optionSelected[idx];
            optionPanels[idx].setBackground(optionSelected[idx] ? ThemeColors.OPTION_SELECTED : ThemeColors.OPTION_BG);
        } else {
            locked = true;
            gameTimer.stop();
            String userAns = String.valueOf((char) ('A' + idx));
            processAnswer(q, userAns, idx);
        }
    }

    private void handleMultiSubmit() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (optionSelected[i] && optionPanels[i].isVisible()) {
                sb.append((char) ('A' + i));
            }
        }
        String userAns = sb.toString();
        if (userAns.isEmpty()) return;

        locked = true;
        gameTimer.stop();
        Question q = questions.get(currentIndex);
        processAnswer(q, userAns, -1);
    }

    private void handleTimeout() {
        if (locked) return;
        locked = true;
        SoundManager.timeout();
        Question q = questions.get(currentIndex);
        comboCount = 0;

        String correct = q.getAnswer();
        for (int i = 0; i < 4; i++) {
            if (correct.indexOf((char) ('A' + i)) >= 0) {
                optionPanels[i].setBackground(ThemeColors.SUCCESS);
                optionLabels[i].setForeground(ThemeColors.TEXT_WHITE);
            }
        }

        try {
            wrongQuestionDAO.insertOrIncrement(user.getId(), q.getId());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        statusLabel.setText("超时！正确答案: " + correct);
        scheduleNextQuestion();
    }

    private void processAnswer(Question q, String userAns, int singleIdx) {
        boolean correct = gameService.isCorrect(q, userAns);

        if (correct) {
            SoundManager.correct();
            correctCount++;
            comboCount++;
            speedBonus += secondsLeft / 2;
            if (comboCount >= 3) comboBonus += 2 * comboCount;

            long elapsed = System.currentTimeMillis() - questionStartTime;
            if (elapsed <= 2000) {
                new Thread(() -> {
                    try { achievementDAO.award(user.getId(), Badge.SPEED_DEMON.key()); }
                    catch (Exception ignored) {}
                }).start();
            }

            if (singleIdx >= 0) {
                optionPanels[singleIdx].setBackground(ThemeColors.SUCCESS);
                optionLabels[singleIdx].setForeground(ThemeColors.TEXT_WHITE);
                optionPanels[singleIdx].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeColors.SUCCESS, 2),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));
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

            // particle burst at the selected option
            if (singleIdx >= 0) {
                Point p = optionPanels[singleIdx].getLocationOnScreen();
                SwingUtilities.convertPointFromScreen(p, this);
                particleEngine.spawnCorrectBurst(p.x + optionPanels[singleIdx].getWidth() / 2.0,
                                                 p.y + optionPanels[singleIdx].getHeight() / 2.0);
            }

            String bonus = "";
            if (comboCount >= 3) {
                bonus = "  combo+" + (2 * comboCount);
                showComboFire(comboCount);
            }
            statusLabel.setText("正确！连对 " + comboCount + " 题 | +" + (Config.baseScore() + secondsLeft / 2) + bonus);
            if (comboCount >= Config.comboThreshold()) comboAchieved = true;
        } else {
            SoundManager.wrong();
            comboCount = 0;

            String correctAns = q.getAnswer();
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

            statusLabel.setText("错误！正确答案: " + correctAns);

            try {
                wrongQuestionDAO.insertOrIncrement(user.getId(), q.getId());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        // Show explanation feedback
        String exp = q.getExplanation();
        if (exp != null && !exp.isEmpty()) {
            gameExplanationLabel.setText("<html><body style='width:380px'> " + escapeHtml(exp) + "</body></html>");
        } else {
            gameExplanationLabel.setText("");
        }
        gameFeedbackLabel.setText(correct ? "回答正确！" : "回答错误！");
        gameFeedbackLabel.setForeground(correct ? ThemeColors.SUCCESS : ThemeColors.DANGER);
        feedbackPanel.setVisible(true);
        revalidate();
        repaint();

        scheduleNextQuestion();
    }

    private void scheduleNextQuestion() {
        if (currentIndex + 1 >= questions.size()) {
            Timer t = new Timer(1500, e -> finishGame(comboAchieved ? "win_combo" :
                (correctCount >= 7 ? "win" : "lose")));
            t.setRepeats(false);
            t.start();
            return;
        }
        Timer t = new Timer(1500, e -> loadQuestion(currentIndex + 1));
        t.setRepeats(false);
        t.start();
    }

    private void finishGame(String result) {
        if (gameTimer != null) gameTimer.stop();

        boolean comboWin = "win_combo".equals(result);
        boolean isWin = comboWin || "win".equals(result);
        if (isWin) SoundManager.victory();

        String rankBefore = user.getRank();
        int allCorrectBonus = (correctCount == questions.size()) ? 30 : 0;
        int scoreEarned = gameService.calculateScore(correctCount, questions.size(), comboWin, rankBefore,
                speedBonus, comboBonus, allCorrectBonus);

        // Daily challenge: double score + streak bonus
        int dailyStreakBonus = 0;
        if (dailyChallenge) {
            scoreEarned *= Config.dailyScoreMultiplier();
            try {
                int streak = dailyChallengeDAO.getStreak(user.getId());
                dailyStreakBonus = streak * 10;
                scoreEarned += dailyStreakBonus;
                dailyChallengeDAO.record(user.getId(), scoreEarned, isWin);
                if (streak + 1 >= 7) {
                    achievementDAO.award(user.getId(), Badge.DAILY_STREAK_7.key());
                }
            } catch (SQLException ignored) {}
        }

        try {
            userService.updateScoreAndRank(user, scoreEarned);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String rankAfter = user.getRank();

        GameHistory gh = new GameHistory();
        gh.setUserId(user.getId());
        gh.setCategory(category);
        gh.setResult(comboWin ? "win_combo" : (isWin ? "win" : "lose"));
        gh.setCorrectCount(correctCount);
        int elapsedSeconds = (int)((System.currentTimeMillis() - gameStartTimeMs) / 1000);
        gh.setTotalTimeSeconds(elapsedSeconds);
        gh.setScoreEarned(scoreEarned);
        gh.setRankBefore(rankBefore);
        gh.setRankAfter(rankAfter);

        try {
            gameHistoryDAO.insert(gh);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        checkAchievements(correctCount, comboWin, rankAfter);

        GameController gc = GameController.getInstance();
        if (isWin) {
            gc.gameWin(correctCount, scoreEarned, comboWin, rankBefore, rankAfter);
        } else {
            gc.gameLose(correctCount, scoreEarned);
        }

        dailyChallenge = false;
    }

    private void checkAchievements(int correctCount, boolean comboWin, String rankAfter) {
        new Thread(() -> {
            try {
                int uid = user.getId();
                // FIRST_WIN & TEN_WINS
                GameHistoryDAO.GameStats stats = gameHistoryDAO.getStats(uid);
                if (stats.wins == 1) achievementDAO.award(uid, Badge.FIRST_WIN.key());
                if (stats.wins >= 10) achievementDAO.award(uid, Badge.TEN_WINS.key());

                // PERFECT_SCORE
                if (correctCount == questions.size())
                    achievementDAO.award(uid, Badge.PERFECT_SCORE.key());

                // COMBO_MASTER
                if (comboWin)
                    achievementDAO.award(uid, Badge.COMBO_MASTER.key());

                // HUNDRED_CORRECT
                if (achievementDAO.totalCorrectAnswers(uid) >= 100)
                    achievementDAO.award(uid, Badge.HUNDRED_CORRECT.key());

                // Rank-based
                int rankIdx = youxi.service.UserService.getRankIndex(rankAfter);
                if (rankIdx >= 2)
                    achievementDAO.award(uid, Badge.GOLD_RANK.key());
                if (rankIdx >= 4)
                    achievementDAO.award(uid, Badge.DIAMOND_RANK.key());

                // CENTURION
                if (user.getTotalScore() >= 1000)
                    achievementDAO.award(uid, Badge.CENTURION.key());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ---------- 动画反馈 ----------

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

    private void showComboFire(int combo) {
        String prev = progressLabel.getText();
        Color prevColor = progressLabel.getForeground();
        Font baseFont = progressLabel.getFont();
        progressLabel.setText(combo + " 连对！");
        progressLabel.setForeground(ThemeColors.GOLD);

        // scale pop: 0.5 → 1.3 → 1.0 over 500ms
        AnimationScheduler.getInstance().animate(300, Easing.EASE_OUT_BACK, t -> {
            float scale = (float) Interpolators.lerpDouble(0.5, 1.3, t);
            progressLabel.setFont(baseFont.deriveFont(baseFont.getSize2D() * scale));
        }, () -> {
            AnimationScheduler.getInstance().animate(200, Easing.EASE_OUT_QUAD, t -> {
                float scale = (float) Interpolators.lerpDouble(1.3, 1.0, t);
                progressLabel.setFont(baseFont.deriveFont(baseFont.getSize2D() * scale));
            }, () -> {
                progressLabel.setText(prev);
                progressLabel.setForeground(prevColor);
                progressLabel.setFont(baseFont);
            });
        });
    }

    private void endGame() {
        if (gameTimer != null) gameTimer.stop();
        GameController.getInstance().goToHome();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
