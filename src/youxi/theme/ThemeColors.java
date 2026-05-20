package youxi.theme;

import java.awt.Color;

/**
 * 全局色彩定义 — Cyber-Neon 电竞风格
 */
public class ThemeColors {

    // ── 深空背景 ──
    public static Color PANEL_BG        = new Color(0x08, 0x0B, 0x1A);
    public static Color SURFACE_BG      = new Color(0x0E, 0x12, 0x24);
    public static Color GLASS_BG        = new Color(0x12, 0x16, 0x30, 200);
    public static Color GLASS_OVERLAY   = new Color(0x08, 0x0B, 0x1A, 120);

    // ── 卡片 ──
    public static Color CARD_BG         = new Color(0x12, 0x16, 0x34, 230);
    public static Color CARD_BORDER     = new Color(0x2A, 0x30, 0x50);

    // ── 霓虹主色 ──
    public static Color PRIMARY         = new Color(0x7C, 0x3A, 0xED);   // 霓虹紫
    public static Color PRIMARY_GLOW    = new Color(0xA7, 0x8B, 0xFA);   // 紫色辉光
    public static Color PRIMARY_DARK    = new Color(0x5B, 0x21, 0xB6);
    public static Color ACCENT          = new Color(0x22, 0xD3, 0xEE);   // 青色
    public static Color ACCENT_GLOW     = new Color(0x67, 0xE8, 0xF9);
    public static Color ROSE            = new Color(0xF4, 0x3F, 0x5E);   // 玫瑰红CTA
    public static Color ROSE_GLOW       = new Color(0xFB, 0x71, 0x85);
    public static Color SUCCESS         = new Color(0x34, 0xD3, 0x99);   // 翠绿
    public static Color DANGER          = new Color(0xEF, 0x44, 0x44);
    public static Color WARNING         = new Color(0xFB, 0xBF, 0x24);   // 琥珀金
    public static Color GOLD            = new Color(0xFB, 0xBF, 0x24);
    public static Color GOLD_GLOW       = new Color(0xFD, 0xE0, 0x47);

    // ── 文本色 ──
    public static Color TEXT_DARK       = new Color(0xE2, 0xE8, 0xF0);
    public static Color TEXT_BODY       = new Color(0xC8, 0xD2, 0xDC);
    public static Color TEXT_MUTED      = new Color(0x94, 0xA3, 0xB8);
    public static Color TEXT_WHITE      = new Color(0xF8, 0xFA, 0xFC);

    // ── 答题专用 ──
    public static Color OPTION_BG       = new Color(0x15, 0x1A, 0x38);
    public static Color OPTION_HOVER    = new Color(0x1E, 0x25, 0x50);
    public static Color OPTION_SELECTED = new Color(0x5B, 0x21, 0xB6, 100);

    // ── 交互色 ──
    public static Color CHECKIN_COLOR   = new Color(0xFB, 0xBF, 0x24);
    public static Color CHECKIN_DONE    = new Color(0x34, 0xD3, 0x99);
    public static Color TEAL            = new Color(0x22, 0xD3, 0xEE);
    public static Color PURPLE          = new Color(0x7C, 0x3A, 0xED);
    public static Color BLUE_GREY       = new Color(0x64, 0x74, 0x8B);

    // ── 学科卡片色 (霓虹系) ──
    public static final Color[][] SUBJECT_COLORS = {
        {new Color(0x7C, 0x3A, 0xED), new Color(0x5B, 0x21, 0xB6)},   // 紫
        {new Color(0x22, 0xD3, 0xEE), new Color(0x06, 0xB6, 0xD4)},   // 青
        {new Color(0x34, 0xD3, 0x99), new Color(0x10, 0xB9, 0x81)},   // 翠绿
        {new Color(0xF4, 0x3F, 0x5E), new Color(0xE1, 0x1D, 0x48)},   // 玫红
        {new Color(0xFB, 0xBF, 0x24), new Color(0xF5, 0x9E, 0x0B)},   // 琥珀
        {new Color(0xA7, 0x8B, 0xFA), new Color(0x8B, 0x5C, 0xF6)},   // 淡紫
        {new Color(0xF4, 0x72, 0xB6), new Color(0xEC, 0x48, 0x99)},   // 粉红
        {new Color(0xEF, 0x44, 0x44), new Color(0xDC, 0x26, 0x26)}    // 深红
    };

    public static Color answerCorrect() { return SUCCESS; }
    public static Color answerWrong()   { return DANGER; }
    public static Color answerCombo()   { return GOLD; }
}
