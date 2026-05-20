package youxi;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import youxi.theme.ThemeManager;
import youxi.util.Config;

public class MainFrame extends JFrame {

    private static MainFrame instance;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JLayeredPane layeredPane;
    private boolean fullscreen = false;
    private Rectangle normalBounds;

    public static MainFrame getInstance() { return instance; }

    public MainFrame() {
        if ("dark".equalsIgnoreCase(Config.get("ui.theme", "dark"))) {
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        ThemeManager.setDark("dark".equalsIgnoreCase(Config.get("ui.theme", "dark")));
        instance = this;

        setTitle("知识竞答 - 通信竞赛");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(640, 480));

        // 全屏或最大化
        if (Config.uiFullscreen()) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setSize(520, 720);
            setLocationRelativeTo(null);
        }

        try {
            setIconImage(javax.imageio.ImageIO.read(new java.io.File("picture_pro/Gemini_Generated_Image_aol6w9aol6w9aol6.png")));
        } catch (Exception ignored) {}

        // F11 全屏切换
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(e -> {
                if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_F11) {
                    toggleFullscreen();
                    return true;
                }
                return false;
            });

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        layeredPane = new JLayeredPane() {
            @Override
            public void doLayout() {
                super.doLayout();
                for (Component c : getComponents()) {
                    if (getLayer(c) == JLayeredPane.DEFAULT_LAYER) {
                        c.setBounds(0, 0, getWidth(), getHeight());
                    }
                }
            }
        };
        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);
        setContentPane(layeredPane);
    }

    private void toggleFullscreen() {
        if (fullscreen) {
            dispose();
            setUndecorated(false);
            setVisible(true);
            if (normalBounds != null) setBounds(normalBounds);
            else setExtendedState(JFrame.MAXIMIZED_BOTH);
            fullscreen = false;
        } else {
            normalBounds = getBounds();
            dispose();
            setUndecorated(true);
            setVisible(true);
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
            if (device.isFullScreenSupported()) {
                device.setFullScreenWindow(this);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            fullscreen = true;
        }
    }

    public void addPanel(JPanel panel, String name) {
        panel.setName(name);
        mainPanel.add(panel, name);
    }

    public void showPanel(String name) {
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(mainPanel, name);
        });
    }

    public JPanel getPanel(String name) {
        for (Component comp : mainPanel.getComponents()) {
            if (name.equals(comp.getName())) return (JPanel) comp;
        }
        return null;
    }

    public CardLayout getCardLayout() { return cardLayout; }
    public JPanel getMainPanel() { return mainPanel; }
}
