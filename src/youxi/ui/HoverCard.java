package youxi.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

import youxi.animation.AnimationScheduler;
import youxi.animation.Easing;
import youxi.animation.Interpolators;

/**
 * A JPanel card with smooth hover/press transitions.
 * Renders a rounded-rect background that lerps between idle and hover colours,
 * a gloss highlight that intensifies on hover, and a subtle press scale.
 */
public class HoverCard extends JPanel {

    private static final int ARC = 16;

    private Color idleColor;
    private Color hoverColor;
    private double hoverT;
    private double pressT;
    private Runnable onClick;

    private AnimationScheduler.AnimationTask hoverTask;
    private AnimationScheduler.AnimationTask pressTask;

    public HoverCard(Color idleColor, Color hoverColor) {
        this.idleColor = idleColor;
        this.hoverColor = hoverColor;
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        initMouse();
    }

    public void setOnClick(Runnable action) {
        this.onClick = action;
    }

    public void setIdleColor(Color c) { this.idleColor = c; }
    public void setHoverColor(Color c) { this.hoverColor = c; }

    private void initMouse() {
        MouseAdapter adapter = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                animateHover(1.0, 250);
            }
            public void mouseExited(MouseEvent e) {
                animateHover(0.0, 200);
                if (pressT > 0) animatePress(0.0, 100);
            }
            public void mousePressed(MouseEvent e) {
                animatePress(0.04, 80);
            }
            public void mouseReleased(MouseEvent e) {
                animatePress(0.0, 400);
                if (contains(e.getPoint()) && onClick != null) {
                    onClick.run();
                }
            }
        };
        addMouseListener(adapter);
    }

    private void animateHover(double target, long durationMs) {
        if (hoverTask != null) hoverTask.cancel();
        double start = hoverT;
        hoverTask = AnimationScheduler.getInstance().animate(durationMs, Easing.EASE_OUT_QUAD, t -> {
            hoverT = Interpolators.lerpDouble(start, target, t);
            repaint();
        });
    }

    private void animatePress(double target, long durationMs) {
        if (pressTask != null) pressTask.cancel();
        double start = pressT;
        Easing easing = target == 0 ? Easing.EASE_OUT_BACK : Easing.EASE_OUT_QUAD;
        pressTask = AnimationScheduler.getInstance().animate(durationMs, easing, t -> {
            pressT = Interpolators.lerpDouble(start, target, t);
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        double scale = 1.0 - pressT;
        int sw = (int) (w * scale), sh = (int) (h * scale);
        int sx = (w - sw) / 2, sy = (h - sh) / 2;

        // Background
        Color bg = Interpolators.lerpColor(idleColor, hoverColor, hoverT);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(sx, sy, sw, sh, ARC, ARC));

        // Gloss highlight
        float glossAlpha = (float) (0.12 + 0.08 * hoverT);
        GradientPaint gloss = new GradientPaint(
            sx, sy, new Color(255, 255, 255, (int) (glossAlpha * 255)),
            sx, sy + sh * 0.5f, new Color(255, 255, 255, 0));
        g2.setPaint(gloss);
        g2.fill(new RoundRectangle2D.Float(sx, sy, sw, sh * 0.7f, ARC, ARC));

        g2.dispose();
        super.paintComponent(g);
    }
}
