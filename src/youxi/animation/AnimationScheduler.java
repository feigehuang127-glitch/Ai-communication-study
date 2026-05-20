package youxi.animation;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Singleton animation scheduler — one shared 16ms Timer drives all animations.
 * Tasks auto-cleanup after completion. All callbacks fire on the EDT.
 */
public class AnimationScheduler {

    private static final AnimationScheduler INSTANCE = new AnimationScheduler();
    private static final int TICK_MS = 16;

    private final Timer timer;
    private final List<AnimationTask> tasks = new CopyOnWriteArrayList<>();
    private long lastTick;

    private AnimationScheduler() {
        lastTick = System.currentTimeMillis();
        timer = new Timer(TICK_MS, this::tick);
        timer.start();
    }

    public static AnimationScheduler getInstance() {
        return INSTANCE;
    }

    /**
     * Schedule an animation.
     *
     * @param durationMs total duration in milliseconds
     * @param easing     easing function applied to the progress [0,1]
     * @param onFrame    called on EDT with eased progress 0→1
     * @return the scheduled task (can be cancelled via task.cancel())
     */
    public AnimationTask animate(long durationMs, DoubleUnaryOperator easing, Consumer<Double> onFrame) {
        AnimationTask task = new AnimationTask(durationMs, easing, onFrame);
        tasks.add(task);
        return task;
    }

    /**
     * Convenience: animate with custom easing, then run onDone on EDT.
     */
    public AnimationTask animate(long durationMs, DoubleUnaryOperator easing, Consumer<Double> onFrame, Runnable onDone) {
        AnimationTask task = new AnimationTask(durationMs, easing, onFrame) {
            @Override
            void finish() {
                super.finish();
                if (onDone != null) onDone.run();
            }
        };
        tasks.add(task);
        return task;
    }

    /**
     * Convenience: animate with EASE_OUT_CUBIC default easing, then run onDone on EDT.
     */
    public AnimationTask animate(long durationMs, Consumer<Double> onFrame, Runnable onDone) {
        AnimationTask task = new AnimationTask(durationMs, Easing.EASE_OUT_CUBIC, onFrame) {
            @Override
            void finish() {
                super.finish();
                if (onDone != null) onDone.run();
            }
        };
        tasks.add(task);
        return task;
    }

    /** Number of currently active tasks. */
    public int activeCount() {
        return tasks.size();
    }

    private void tick(ActionEvent e) {
        long now = System.currentTimeMillis();
        long delta = now - lastTick;
        lastTick = now;

        for (AnimationTask task : tasks) {
            task.tick(delta);
        }
        tasks.removeIf(t -> t.state == TaskState.DONE);
    }

    private enum TaskState { RUNNING, CANCELLED, DONE }

    public static class AnimationTask {
        final long durationMs;
        final DoubleUnaryOperator easing;
        final Consumer<Double> onFrame;
        long elapsed;
        TaskState state = TaskState.RUNNING;

        AnimationTask(long durationMs, DoubleUnaryOperator easing, Consumer<Double> onFrame) {
            this.durationMs = Math.max(1, durationMs);
            this.easing = easing;
            this.onFrame = onFrame;
        }

        void tick(long delta) {
            if (state != TaskState.RUNNING) return;
            elapsed += delta;
            double rawT = Math.min(1.0, (double) elapsed / durationMs);
            double easedT = easing.applyAsDouble(rawT);
            SwingUtilities.invokeLater(() -> onFrame.accept(easedT));
            if (rawT >= 1.0) {
                finish();
            }
        }

        void finish() {
            state = TaskState.DONE;
        }

        public void cancel() {
            state = TaskState.CANCELLED;
        }

        public boolean isRunning() {
            return state == TaskState.RUNNING;
        }
    }
}
