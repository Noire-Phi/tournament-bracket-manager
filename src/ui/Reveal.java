package ui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Wraps one block so it can rise and fade in without touching layout.
 * Only position (a few pixels) and opacity change, as UI_GUIDE section 6 asks.
 */
public class Reveal extends JPanel {
    private float alpha = 1f;
    private int dy;

    public Reveal(JComponent content) {
        super(new BorderLayout());
        setOpaque(false);
        add(content, BorderLayout.CENTER);
    }

    /** Hides the block now, then rises it into place after delayMs. Does nothing with reduced motion. */
    public void play(int risePx, int durationMs, int delayMs) {
        if (Motion.reduced()) return;
        apply(0, risePx);
        Motion.after(delayMs, () -> Motion.animate(durationMs, p -> apply(p, risePx), null));
    }

    private void apply(double progress, int risePx) {
        alpha = (float) progress;
        dy = (int) Math.round((1 - progress) * risePx);
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        if (alpha >= 1f && dy == 0) {
            super.paint(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(0, dy);
        g2.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f, Math.min(1f, alpha))));
        super.paint(g2);
        g2.dispose();
    }
}
