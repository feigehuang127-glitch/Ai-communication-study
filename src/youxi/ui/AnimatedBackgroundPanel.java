package youxi.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import youxi.theme.ThemeColors;

/**
 * Base panel that replaces static white-overlay backgrounds with animated floating
 * gradient orbs, a configurable dark overlay, and smooth background-image rendering.
 *
 * <p>Subclasses call {@code super.paintComponent(g)} first, then add their own
 * painting on top.</p>
 */
public abstract class AnimatedBackgroundPanel extends JPanel {

    public enum OrbTheme {
        AMBER   (new float[]{30f,  50f}),
        CYAN    (new float[]{180f, 220f}),
        VIOLET  (new float[]{260f, 290f}),
        EMERALD (new float[]{140f, 170f}),
        BLUE    (new float[]{200f, 240f}),
        INDIGO  (new float[]{240f, 270f}),
        SLATE   (new float[]{210f, 230f}),
        ROSE    (new float[]{0f,   15f});

        final float hueLow, hueHigh;
        OrbTheme(float[] range) { this.hueLow = range[0]; this.hueHigh = range[1]; }
    }

    private static final int ORB_COUNT = 4;
    private static final float ORB_RADIUS_MIN = 120f;
    private static final float ORB_RADIUS_MAX = 250f;
    private static final int ORB_ALPHA_MAX = 35;
    private static final int TIMER_MS = 33;

    private BufferedImage bgImage;
    private final List<FloatingOrb> orbs = new ArrayList<>(ORB_COUNT);
    private final Timer animTimer;
    private final OrbTheme orbTheme;
    private final Color overlayColor;
    private final Random rng = new Random();

    protected AnimatedBackgroundPanel(String bgImagePath, OrbTheme orbTheme) {
        this(bgImagePath, orbTheme, new Color(0, 0, 0, 70));
    }

    protected AnimatedBackgroundPanel(String bgImagePath, OrbTheme orbTheme, Color overlayColor) {
        this.orbTheme = orbTheme;
        this.overlayColor = overlayColor;
        setOpaque(false);

        try {
            bgImage = ImageIO.read(new File(bgImagePath));
        } catch (IOException e) {
            System.err.println("背景图加载失败: " + bgImagePath + " — " + e.getMessage());
        }

        for (int i = 0; i < ORB_COUNT; i++) {
            orbs.add(new FloatingOrb());
        }

        animTimer = new Timer(TIMER_MS, this::onTimerTick);
    }

    /** Start the orb animation. Called when the panel becomes visible. */
    @Override
    public void addNotify() {
        super.addNotify();
        animTimer.start();
    }

    /** Stop the orb animation when the panel is removed. */
    @Override
    public void removeNotify() {
        super.removeNotify();
        animTimer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        drawBackgroundImage(g2, w, h);
        drawFloatingOrbs(g2, w, h);
        drawOverlay(g2, w, h);
    }

    /** Override to customise the overlay (e.g. gradient instead of flat). */
    protected void drawOverlay(Graphics2D g2, int w, int h) {
        g2.setColor(overlayColor);
        g2.fillRect(0, 0, w, h);
    }

    private void drawBackgroundImage(Graphics2D g2, int w, int h) {
        if (bgImage == null) return;
        double imgRatio = (double) bgImage.getWidth() / bgImage.getHeight();
        double panelRatio = (double) w / h;
        int drawW, drawH, drawX, drawY;
        if (imgRatio > panelRatio) {
            drawH = h;
            drawW = (int) (h * imgRatio);
            drawX = -(drawW - w) / 2;
            drawY = 0;
        } else {
            drawW = w;
            drawH = (int) (w / imgRatio);
            drawX = 0;
            drawY = -(drawH - h) / 2;
        }
        g2.drawImage(bgImage, drawX, drawY, drawW, drawH, null);
    }

    private void drawFloatingOrbs(Graphics2D g2, int w, int h) {
        for (FloatingOrb orb : orbs) {
            float hue = orbTheme.hueLow + (orbTheme.hueHigh - orbTheme.hueLow) * orb.hueShift;
            float saturation = 0.15f + 0.05f * (float) Math.sin(orb.phase);
            int alpha = (int) (ORB_ALPHA_MAX * (0.7f + 0.3f * (float) Math.sin(orb.phase * 1.7)));

            Color inner = new Color(Color.HSBtoRGB(hue / 360f, saturation, 0.12f));
            Color outer = new Color(0, 0, 0, 0);

            float r = orb.radius * (0.9f + 0.1f * (float) Math.sin(orb.phase * 0.7));
            float[] dist = {0f, 1f};
            Color[] colors = {
                new Color(inner.getRed(), inner.getGreen(), inner.getBlue(), alpha),
                outer
            };

            RadialGradientPaint paint = new RadialGradientPaint(
                orb.x, orb.y, r, dist, colors
            );

            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            g2.setPaint(paint);
            g2.fillOval((int) (orb.x - r), (int) (orb.y - r), (int) (r * 2), (int) (r * 2));
            g2.setComposite(old);
        }
    }

    private void onTimerTick(ActionEvent e) {
        int w = getWidth(), h = getHeight();
        for (FloatingOrb orb : orbs) {
            orb.x += orb.dx;
            orb.y += orb.dy;
            orb.phase += 0.025;

            float r = orb.radius;
            if (orb.x < -r) orb.x = w + r;
            if (orb.x > w + r) orb.x = -r;
            if (orb.y < -r) orb.y = h + r;
            if (orb.y > h + r) orb.y = -r;
        }
        repaint();
    }

    private class FloatingOrb {
        float x, y, radius, dx, dy, phase, hueShift;

        FloatingOrb() {
            // Initialised lazily on first paint with actual dimensions
            x = rng.nextFloat() * 800;
            y = rng.nextFloat() * 600;
            radius = ORB_RADIUS_MIN + rng.nextFloat() * (ORB_RADIUS_MAX - ORB_RADIUS_MIN);
            dx = 0.10f + rng.nextFloat() * 0.35f * (rng.nextBoolean() ? 1 : -1);
            dy = 0.08f + rng.nextFloat() * 0.25f * (rng.nextBoolean() ? 1 : -1);
            phase = rng.nextFloat() * (float) (2 * Math.PI);
            hueShift = rng.nextFloat();
        }
    }
}
