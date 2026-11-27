package ui;

import java.util.function.DoubleConsumer;
import javax.swing.Timer;

/** Small animation helper: one easing, driven by a Swing Timer on the Event Dispatch Thread. */
public final class Motion {
    private Motion() {}

    /** True when the user asked for no motion (run with -Dtbm.reduceMotion=true). */
    public static boolean reduced() {
        return Boolean.getBoolean("tbm.reduceMotion");
    }

    /** Ease-out cubic: fast start, soft landing. t is 0..1. */
    public static double ease(double t) {
        double u = 1 - t;
        return 1 - u * u * u;
    }

    /** Runs r after delayMs. With reduced motion, or no delay, it runs at once. */
    public static void after(int delayMs, Runnable r) {
        if (reduced() || delayMs <= 0) {
            r.run();
            return;
        }
        Timer t = new Timer(delayMs, e -> r.run());
        t.setRepeats(false);
        t.start();
    }

    /**
     * Calls step with eased progress 0..1 every ~16ms for durationMs, then runs done.
     * With reduced motion it jumps straight to 1.
     */
    public static void animate(int durationMs, DoubleConsumer step, Runnable done) {
        if (reduced() || durationMs <= 0) {
            step.accept(1);
            if (done != null) done.run();
            return;
        }
        long start = System.nanoTime();
        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            double t = Math.min(1.0, (System.nanoTime() - start) / (durationMs * 1_000_000.0));
            step.accept(ease(t));
            if (t >= 1.0) {
                timer.stop();
                if (done != null) done.run();
            }
        });
        step.accept(0);
        timer.start();
    }
}
