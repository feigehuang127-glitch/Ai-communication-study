package youxi.animation;

import java.awt.*;

public final class Interpolators {

    private Interpolators() {}

    public static Color lerpColor(Color c1, Color c2, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
            (int) (c1.getRed()   + (c2.getRed()   - c1.getRed())   * t),
            (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t),
            (int) (c1.getBlue()  + (c2.getBlue()  - c1.getBlue())  * t),
            (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * t)
        );
    }

    public static float lerpFloat(float a, float b, double t) {
        t = Math.max(0, Math.min(1, t));
        return (float) (a + (b - a) * t);
    }

    public static double lerpDouble(double a, double b, double t) {
        t = Math.max(0, Math.min(1, t));
        return a + (b - a) * t;
    }

    public static int lerpInt(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        return (int) (a + (b - a) * t);
    }
}
