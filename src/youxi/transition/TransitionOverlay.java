package youxi.transition;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class TransitionOverlay extends JComponent {

    private final BufferedImage snapshot;
    private float alpha = 1.0f;
    private float scale = 1.0f;
    private Timer timer;
    private Runnable onDone;
    private int elapsed;
    private static final int DURATION = 350;
    private static final int INTERVAL = 16;
    private static final float MIN_SCALE = 0.96f;

    public TransitionOverlay(BufferedImage snapshot) {
        this.snapshot = snapshot;
        setOpaque(false);
    }

    public void start(Runnable onDone) {
        this.onDone = onDone;
        this.elapsed = 0;
        this.alpha = 1.0f;
        this.scale = 1.0f;
        timer = new Timer(INTERVAL, e -> {
            elapsed += INTERVAL;
            if (elapsed >= DURATION) {
                alpha = 0f;
                scale = MIN_SCALE;
                timer.stop();
                repaint();
                if (onDone != null) onDone.run();
            } else {
                float t = (float) elapsed / DURATION;
                float eased = (float) Math.pow(t, 3); // ease-out cubic
                alpha = 1f - eased;
                scale = 1f - (1f - MIN_SCALE) * eased; // 1.0 → 0.96
                repaint();
            }
        });
        timer.setRepeats(true);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (snapshot == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int w = getWidth();
        int h = getHeight();
        int sw = (int) (w * scale);
        int sh = (int) (h * scale);
        int sx = (w - sw) / 2;
        int sy = (h - sh) / 2;

        g2.drawImage(snapshot, sx, sy, sw, sh, null);
        g2.dispose();
    }
}
