package youxi.theme;

import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import youxi.MainFrame;

public class ThemeManager {

    private static boolean dark = true;

    public static boolean isDark() { return dark; }

    public static void setDark(boolean d) {
        if (dark == d) return;
        dark = d;
        applyThemeColors();
    }

    public static void toggle() {
        dark = !dark;
        saveTheme();
        applyThemeColors();
        try {
            if (dark) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
        } catch (Exception e) {
            System.err.println("[ThemeManager] L&F 切换失败: " + e.getMessage());
        }
        MainFrame frame = MainFrame.getInstance();
        if (frame != null) {
            SwingUtilities.updateComponentTreeUI(frame);
        }
    }

    private static void saveTheme() {
        File f = new File("config.properties");
        Properties p = new Properties();
        if (f.exists()) {
            try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
                p.load(r);
            } catch (IOException ignored) {}
        }
        p.setProperty("ui.theme", dark ? "dark" : "light");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            p.store(w, null);
        } catch (IOException ignored) {}
    }

    private static void applyThemeColors() {
        if (dark) {
            // Cyber-Neon Dark
            ThemeColors.PANEL_BG        = new Color(0x08, 0x0B, 0x1A);
            ThemeColors.SURFACE_BG      = new Color(0x0E, 0x12, 0x24);
            ThemeColors.GLASS_BG        = new Color(0x12, 0x16, 0x30, 200);
            ThemeColors.GLASS_OVERLAY   = new Color(0x08, 0x0B, 0x1A, 120);
            ThemeColors.CARD_BG         = new Color(0x12, 0x16, 0x34, 230);
            ThemeColors.CARD_BORDER     = new Color(0x2A, 0x30, 0x50);
            ThemeColors.PRIMARY         = new Color(0x7C, 0x3A, 0xED);
            ThemeColors.PRIMARY_GLOW    = new Color(0xA7, 0x8B, 0xFA);
            ThemeColors.PRIMARY_DARK    = new Color(0x5B, 0x21, 0xB6);
            ThemeColors.ACCENT          = new Color(0x22, 0xD3, 0xEE);
            ThemeColors.ACCENT_GLOW     = new Color(0x67, 0xE8, 0xF9);
            ThemeColors.ROSE            = new Color(0xF4, 0x3F, 0x5E);
            ThemeColors.ROSE_GLOW       = new Color(0xFB, 0x71, 0x85);
            ThemeColors.GOLD            = new Color(0xFB, 0xBF, 0x24);
            ThemeColors.GOLD_GLOW       = new Color(0xFD, 0xE0, 0x47);
            ThemeColors.SUCCESS         = new Color(0x34, 0xD3, 0x99);
            ThemeColors.TEXT_DARK       = new Color(0xE2, 0xE8, 0xF0);
            ThemeColors.TEXT_BODY       = new Color(0xC8, 0xD2, 0xDC);
            ThemeColors.TEXT_MUTED      = new Color(0x94, 0xA3, 0xB8);
            ThemeColors.OPTION_BG       = new Color(0x15, 0x1A, 0x38);
            ThemeColors.OPTION_HOVER    = new Color(0x1E, 0x25, 0x50);
            ThemeColors.OPTION_SELECTED = new Color(0x5B, 0x21, 0xB6, 100);
            ThemeColors.CHECKIN_DONE    = new Color(0x34, 0xD3, 0x99);
            ThemeColors.CHECKIN_COLOR   = new Color(0xFB, 0xBF, 0x24);
        } else {
            // Cyber-Neon Light (subdued)
            ThemeColors.PANEL_BG        = new Color(0xF0, 0xF0, 0xFA);
            ThemeColors.SURFACE_BG      = new Color(0xE8, 0xE8, 0xF5);
            ThemeColors.GLASS_BG        = new Color(255, 255, 255, 180);
            ThemeColors.GLASS_OVERLAY   = new Color(0, 0, 0, 40);
            ThemeColors.CARD_BG         = new Color(255, 255, 255, 220);
            ThemeColors.CARD_BORDER     = new Color(0xD4, 0xD4, 0xE4);
            ThemeColors.PRIMARY         = new Color(0x6D, 0x28, 0xD9);
            ThemeColors.PRIMARY_GLOW    = new Color(0x8B, 0x5C, 0xF6);
            ThemeColors.PRIMARY_DARK    = new Color(0x5B, 0x21, 0xB6);
            ThemeColors.ACCENT          = new Color(0x08, 0x91, 0xB2);
            ThemeColors.ACCENT_GLOW     = new Color(0x22, 0xD3, 0xEE);
            ThemeColors.ROSE            = new Color(0xE1, 0x1D, 0x48);
            ThemeColors.ROSE_GLOW       = new Color(0xFB, 0x71, 0x85);
            ThemeColors.GOLD            = new Color(0xD9, 0x77, 0x06);
            ThemeColors.GOLD_GLOW       = new Color(0xFB, 0xBF, 0x24);
            ThemeColors.SUCCESS         = new Color(0x10, 0xB9, 0x81);
            ThemeColors.TEXT_DARK       = new Color(0x1E, 0x29, 0x3B);
            ThemeColors.TEXT_BODY       = new Color(0x47, 0x55, 0x69);
            ThemeColors.TEXT_MUTED      = new Color(0x94, 0xA3, 0xB8);
            ThemeColors.OPTION_BG       = Color.WHITE;
            ThemeColors.OPTION_HOVER    = new Color(0xED, 0xE9, 0xFE);
            ThemeColors.OPTION_SELECTED = new Color(0xDD, 0xD6, 0xFE);
            ThemeColors.CHECKIN_DONE    = new Color(0xA7, 0xF3, 0xD0);
            ThemeColors.CHECKIN_COLOR   = new Color(0xFD, 0xE0, 0x47);
        }
    }
}
