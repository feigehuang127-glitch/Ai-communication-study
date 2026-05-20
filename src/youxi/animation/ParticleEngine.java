package youxi.animation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight particle system. Call spawn* methods to create particles,
 * update(deltaMs) each frame, then render(Graphics2D) to draw.
 */
public class ParticleEngine {

    private static final double GRAVITY = 80.0; // px/s²
    private static final Random RNG = new Random();

    private final List<Particle> particles = new CopyOnWriteArrayList<>();

    public void spawnBurst(int count, double cx, double cy, Color baseColor,
                           double speed, long lifeMs) {
        for (int i = 0; i < count; i++) {
            double angle = RNG.nextDouble() * 2 * Math.PI;
            double spd = speed * (0.6 + RNG.nextDouble() * 0.8);
            double vx = Math.cos(angle) * spd;
            double vy = Math.sin(angle) * spd;
            double radius = 2.5 + RNG.nextDouble() * 4.5;

            Color color = varyColor(baseColor, 0.15f);
            particles.add(new Particle(cx, cy, vx, vy, radius, color, lifeMs));
        }
    }

    public void spawnConfetti(int count, double x, double y, double width, Color... colors) {
        if (colors == null || colors.length == 0) {
            colors = new Color[] { Color.WHITE, Color.YELLOW, Color.CYAN, Color.MAGENTA };
        }
        for (int i = 0; i < count; i++) {
            double px = x + RNG.nextDouble() * width;
            double vx = -60 + RNG.nextDouble() * 120;
            double vy = -180 - RNG.nextDouble() * 200;
            double radius = 3 + RNG.nextDouble() * 4;
            Color color = colors[RNG.nextInt(colors.length)];
            particles.add(new Particle(px, y, vx, vy, radius, color, 900 + RNG.nextInt(600)));
        }
    }

    /** Spawn a burst for correct answer at the center of a component. */
    public void spawnCorrectBurst(double cx, double cy) {
        spawnBurst(20, cx, cy, new Color(0xFFD700), 200, 900);
        spawnBurst(8, cx, cy, Color.WHITE, 140, 700);
    }

    /** Spawn a subtle burst for combo streaks. */
    public void spawnComboBurst(double cx, double cy) {
        spawnBurst(8, cx, cy, new Color(0xFFD700), 120, 700);
    }

    public void update(double deltaMs) {
        double dt = deltaMs / 1000.0;
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.life -= deltaMs;
            if (p.life <= 0) {
                particles.remove(p);
                continue;
            }
            p.vy += GRAVITY * dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vx *= 0.995;
        }
    }

    public void render(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Particle p : particles) {
            double alpha = p.life / p.maxLife;
            double r = p.radius * (0.4 + 0.6 * alpha);
            int a = (int) (255 * alpha);
            Color c = new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), a);
            g2.setColor(c);
            int d = (int) (r * 2);
            g2.fillOval((int) (p.x - r), (int) (p.y - r), d, d);
        }
    }

    public int particleCount() {
        return particles.size();
    }

    public void clear() {
        particles.clear();
    }

    private static Color varyColor(Color base, float amount) {
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        hsb[0] += (RNG.nextFloat() - 0.5f) * amount;
        hsb[1] += (RNG.nextFloat() - 0.5f) * amount;
        hsb[2] += (RNG.nextFloat() - 0.5f) * amount;
        hsb[0] = Math.max(0, Math.min(1, hsb[0]));
        hsb[1] = Math.max(0, Math.min(1, hsb[1]));
        hsb[2] = Math.max(0.3f, Math.min(1, hsb[2]));
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    // -- inner class ----------------------------------------------------------

    public static class Particle {
        double x, y, vx, vy;
        double radius;
        double life, maxLife;
        Color color;

        Particle(double x, double y, double vx, double vy, double radius, Color color, long lifeMs) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.color = color;
            this.life = lifeMs;
            this.maxLife = lifeMs;
        }
    }
}
