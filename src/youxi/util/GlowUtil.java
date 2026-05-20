package youxi.util;

import java.awt.*;
import javax.swing.*;

/** 霓虹发光文字绘制工具 */
public class GlowUtil {

    /** 绘制带发光效果的文字 */
    public static void drawGlowText(Graphics2D g2, String text, int x, int y, Color core, Color glow, float glowSize) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // 外层光晕
        Composite orig = g2.getComposite();
        for (int i = (int) glowSize; i >= 1; i--) {
            float alpha = 0.15f * (i / glowSize);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(glow);
            g2.setStroke(new BasicStroke(i * 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Font f = g2.getFont();
            g2.setFont(f.deriveFont(f.getSize2D() + i * 0.5f));
            g2.drawString(text, x, y);
            g2.setFont(f);
        }
        // 核心文字
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2.setColor(core);
        g2.drawString(text, x, y);
        g2.setComposite(orig);
    }

    /** 绘制带发光边框的圆角矩形 */
    public static void drawGlowBorder(Graphics2D g2, int x, int y, int w, int h, int arc, Color border, Color glow, float glowWidth) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Composite orig = g2.getComposite();
        // 外层辉光
        for (int i = (int) glowWidth; i >= 1; i--) {
            float alpha = 0.12f * (i / glowWidth);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(glow);
            g2.setStroke(new BasicStroke(i * 2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(x - i, y - i, w + i * 2, h + i * 2, arc + i, arc + i);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(border);
        g2.drawRoundRect(x, y, w, h, arc, arc);
        g2.setComposite(orig);
    }

    /** 给已有 JLabel 的文字添加发光 */
    public static void applyLabelGlow(JLabel label, Color glowColor) {
        label.setForeground(glowColor);
        label.putClientProperty("glow", glowColor);
        label.putClientProperty("glowSize", 3f);
    }

    /** 创建带发光的大标题 */
    public static JLabel glowTitle(String text, Color core, Color glow, float fontSize) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("微软雅黑", Font.BOLD, (int) fontSize));
        label.setForeground(core);
        label.putClientProperty("glow", glow);
        label.putClientProperty("glowSize", 4f);
        return label;
    }

    /**
     * Draw text with a pulsing glow whose radius oscillates via sin(pulsePhase * 2π).
     * pulsePhase should tick between 0 and 1 over the desired cycle period.
     */
    public static void drawPulsingGlowText(Graphics2D g2, String text, int x, int y,
                                            Color core, Color glow, float baseGlowSize, double pulsePhase) {
        float size = baseGlowSize + 0.6f * baseGlowSize * (float) Math.sin(pulsePhase * 2 * Math.PI);
        drawGlowText(g2, text, x, y, core, glow, Math.max(1f, size));
    }

    /**
     * Draw a rounded-rect border with a pulsing glow.
     */
    public static void drawPulsingGlowBorder(Graphics2D g2, int x, int y, int w, int h, int arc,
                                              Color border, Color glow, float baseGlowWidth, double pulsePhase) {
        float width = baseGlowWidth + 0.5f * baseGlowWidth * (float) Math.sin(pulsePhase * 2 * Math.PI);
        drawGlowBorder(g2, x, y, w, h, arc, border, glow, Math.max(1f, width));
    }
}
