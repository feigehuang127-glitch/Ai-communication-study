package youxi.animation;

import java.util.function.DoubleUnaryOperator;

public enum Easing implements DoubleUnaryOperator {
    LINEAR {
        public double applyAsDouble(double t) { return clamp(t); }
    },
    EASE_IN_QUAD {
        public double applyAsDouble(double t) { t = clamp(t); return t * t; }
    },
    EASE_OUT_QUAD {
        public double applyAsDouble(double t) { t = clamp(t); return 1 - (1 - t) * (1 - t); }
    },
    EASE_IN_OUT_QUAD {
        public double applyAsDouble(double t) {
            t = clamp(t);
            return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
        }
    },
    EASE_OUT_CUBIC {
        public double applyAsDouble(double t) { t = clamp(t); return 1 - Math.pow(1 - t, 3); }
    },
    EASE_IN_OUT_CUBIC {
        public double applyAsDouble(double t) {
            t = clamp(t);
            return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
        }
    },
    EASE_OUT_BACK {
        private static final double C1 = 1.70158;
        private static final double C3 = C1 + 1;
        public double applyAsDouble(double t) {
            t = clamp(t);
            return 1 + C3 * Math.pow(t - 1, 3) + C1 * Math.pow(t - 1, 2);
        }
    },
    EASE_OUT_ELASTIC {
        public double applyAsDouble(double t) {
            t = clamp(t);
            if (t == 0 || t == 1) return t;
            return Math.pow(2, -10 * t) * Math.sin((t - 0.075) * (2 * Math.PI) / 0.3) + 1;
        }
    },
    EASE_IN_OUT_BACK {
        private static final double C1 = 1.70158;
        private static final double C2 = C1 * 1.525;
        public double applyAsDouble(double t) {
            t = clamp(t);
            return t < 0.5
                ? (Math.pow(2 * t, 2) * ((C2 + 1) * 2 * t - C2)) / 2
                : (Math.pow(2 * t - 2, 2) * ((C2 + 1) * (t * 2 - 2) + C2) + 2) / 2;
        }
    },
    EASE_OUT_BOUNCE {
        public double applyAsDouble(double t) {
            t = clamp(t);
            double n1 = 7.5625, d1 = 2.75;
            if (t < 1 / d1) return n1 * t * t;
            else if (t < 2 / d1) { t -= 1.5 / d1; return n1 * t * t + 0.75; }
            else if (t < 2.5 / d1) { t -= 2.25 / d1; return n1 * t * t + 0.9375; }
            else { t -= 2.625 / d1; return n1 * t * t + 0.984375; }
        }
    },
    EASE_IN_BOUNCE {
        public double applyAsDouble(double t) {
            return 1 - EASE_OUT_BOUNCE.applyAsDouble(1 - clamp(t));
        }
    };

    private static double clamp(double t) {
        return Math.max(0, Math.min(1, t));
    }
}
