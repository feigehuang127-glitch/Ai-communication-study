package youxi.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import youxi.animation.AnimationScheduler;
import youxi.animation.Easing;
import youxi.animation.Interpolators;

/**
 * A JButton with press-bounce animation and a release flash.
 * Scale goes 1.0→0.97 on press, then 0.97→1.02→1.0 on release (bounce).
 */
public class HoverButton extends JButton {

    private double pressT;
    private double flashAlpha;
    private AnimationScheduler.AnimationTask pressTask;
    private AnimationScheduler.AnimationTask flashTask;

    public HoverButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                animatePress(0.03, 80);
            }
            public void mouseReleased(MouseEvent e) {
                animatePress(0.0, 400);
                if (contains(e.getPoint())) {
                    triggerFlash();
                }
            }
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

    private void triggerFlash() {
        flashAlpha = 0.4;
        if (flashTask != null) flashTask.cancel();
        flashTask = AnimationScheduler.getInstance().animate(200, Easing.EASE_OUT_QUAD, t -> {
            flashAlpha = Interpolators.lerpDouble(0.4, 0.0, t);
            if (t >= 1.0) flashAlpha = 0;
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

        g2.translate(sx, sy);
        g2.scale(scale, scale);

        // Draw background
        if (getBackground() != null) {
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w, h, 12, 12);
        }

        // Flash overlay
        if (flashAlpha > 0.01) {
            g2.setColor(new Color(255, 255, 255, (int) (flashAlpha * 255)));
            g2.fillRoundRect(0, 0, w, h, 12, 12);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
