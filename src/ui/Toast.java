package ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.Timer;

/**
 * Notification label on the frame's glass pane, bottom center, shown for 4 seconds.
 * Rounded PANEL_DEEP box: amber outline for success, DANGER_TEXT outline for errors. Use Toast.success(root, msg) or Toast.error(root, msg).
 */
public final class Toast extends JLabel {
    private static final String KEY = "tbm.toast";
    private static final int SHOW_MS = 4000;
    private static final int RADIUS = 16;

    /** Glass pane that lets every mouse event through to the page below. */
    private static final class Glass extends JPanel {
        Glass() {
            super(null);
            setOpaque(false);
        }

        @Override
        public boolean contains(int x, int y) {
            return false;
        }
    }

    private final JRootPane root;
    private final Glass glass = new Glass();
    private final Timer hideTimer = new Timer(SHOW_MS, e -> fadeOut());
    private Color accent = Theme.WIN;
    private float alpha;
    private int slide;
    private int generation;

    private Toast(JRootPane root) {
        this.root = root;
        hideTimer.setRepeats(false);
        setFont(Theme.body(Theme.SIZE_M));
        setForeground(Theme.INK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_M, Theme.SPACE_L,
                Theme.SPACE_M, Theme.SPACE_L));
        glass.add(this);
        glass.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                place();
            }
        });
    }

    public static void success(JRootPane root, String message) {
        show(root, message, false);
    }

    public static void error(JRootPane root, String message) {
        show(root, message, true);
    }

    public static void show(JRootPane root, String message, boolean error) {
        Toast t = (Toast) root.getClientProperty(KEY);
        if (t == null) {
            t = new Toast(root);
            root.putClientProperty(KEY, t);
        }
        t.display(message, error);
    }

    private void display(String message, boolean error) {
        generation++;
        accent = error ? Theme.DANGER_TEXT : Theme.WIN;
        setText(message);
        getAccessibleContext().setAccessibleName((error ? "Error: " : "Success: ") + message);
        setSize(getPreferredSize());
        if (root.getGlassPane() != glass) root.setGlassPane(glass);
        glass.setVisible(true);
        hideTimer.restart();
        Motion.animate(Theme.MOTION_MS, p -> {
            alpha = (float) p;
            slide = (int) Math.round((1 - p) * Theme.SPACE_L);
            place();
            glass.repaint();
        }, null);
    }

    private void fadeOut() {
        int gen = generation;
        Motion.animate(Theme.MOTION_MS, p -> {
            alpha = (float) (1 - p);
            glass.repaint();
        }, () -> {
            if (gen == generation) glass.setVisible(false);
        });
    }

    private void place() {
        Dimension d = getPreferredSize();
        int x = (glass.getWidth() - d.width) / 2;
        int y = glass.getHeight() - d.height - Theme.SPACE_XL + slide;
        setBounds(x, y, d.width, d.height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f, Math.min(1f, alpha))));
        int w = getWidth(), h = getHeight();
        g2.setColor(Theme.PANEL_DEEP);
        g2.fillRoundRect(0, 0, w, h, 2 * RADIUS, 2 * RADIUS);
        g2.setColor(accent);
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 2 * RADIUS, 2 * RADIUS);
        super.paintComponent(g2);
        g2.dispose();
    }
}
