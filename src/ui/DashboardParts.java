package ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Custom-painted pieces of the Dashboard. Each exposes the progress values the page animates. */
final class DashboardParts {
    private DashboardParts() {}

    private static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    private static float clamp01(double v) {
        return (float) Math.max(0, Math.min(1, v));
    }

    /** A custom-painted component that still gives screen readers an accessible name. */
    abstract static class Painted extends JComponent {
        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) accessibleContext = new AccessibleJComponent() { };
            return accessibleContext;
        }
    }

    /** The big round number (display 112, amber). It scales in from 160%. */
    static final class BigNumber extends Painted {
        private static final double START_SCALE = 1.6;
        private String text = "-";
        private double scale = 1;
        private float alpha = 1f;

        BigNumber() {
            setFont(Theme.display(Theme.SIZE_BIG));
        }

        void setText(String text) {
            this.text = text;
            getAccessibleContext().setAccessibleName("Round " + text);
            revalidate();
            repaint();
        }

        void slam(int delayMs) {
            if (Motion.reduced()) return;
            set(START_SCALE, 0f);
            Motion.after(delayMs, () -> Motion.animate(Theme.MOTION_SLOW_MS,
                    p -> set(START_SCALE - (START_SCALE - 1) * p, (float) p), null));
        }

        private void set(double scale, float alpha) {
            this.scale = scale;
            this.alpha = alpha;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            // The display face has a tall line box; trim it so "of 4" can sit on the baseline.
            return new Dimension(fm.stringWidth(text) + Theme.SPACE_S, fm.getAscent() - fm.getDescent());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setComposite(AlphaComposite.SrcOver.derive(clamp01(alpha)));
            g2.setFont(getFont());
            g2.setColor(Theme.PRIMARY);
            g2.translate(0, getHeight());
            g2.scale(scale, scale);
            g2.drawString(text, 0, 0);
            g2.dispose();
        }
    }

    /** One segment per round: done rounds amber, the current round INK, later rounds RAISE. Fills from the left. */
    static final class SegmentBar extends Painted {
        private static final int HEIGHT = 10;
        private static final int GAP = 6;
        private int total;
        private int done;
        private int current = -1;
        private double shown = 1;

        void set(int total, int done, int current) {
            this.total = total;
            this.done = done;
            this.current = current;
            getAccessibleContext().setAccessibleName(done + " of " + total + " rounds complete");
            repaint();
        }

        void fill(int delayMs, int durationMs) {
            if (Motion.reduced()) return;
            shown = 0;
            repaint();
            Motion.after(delayMs, () -> Motion.animate(durationMs, p -> {
                shown = p;
                repaint();
            }, null));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(120, HEIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (total == 0) return;
            Graphics2D g2 = smooth(g);
            g2.clipRect(0, 0, (int) Math.round(getWidth() * shown), getHeight());
            int seg = (getWidth() - GAP * (total - 1)) / total;
            for (int i = 0; i < total; i++) {
                g2.setColor(i < done ? Theme.PRIMARY : i == current ? Theme.INK : Theme.RAISE);
                g2.fillRoundRect(i * (seg + GAP), 0, seg, HEIGHT, HEIGHT, HEIGHT);
            }
            g2.dispose();
        }
    }

    /** NAME vs NAME with a record line under each. Names slide in from their own sides, then the badge pops. */
    static final class Versus extends Painted {
        private static final int SLIDE_PX = 48;
        private static final float NAME_SIZE = 44;
        private String left = "", right = "", leftNote = "", rightNote = "";
        private Color nameColor = Theme.INK;
        private Color badgeFill = Theme.PRIMARY;
        private Color badgeInk = Theme.ON_PRIMARY;
        private double names = 1;
        private double badge = 1;

        void setColors(Color name, Color fill, Color ink) {
            nameColor = name;
            badgeFill = fill;
            badgeInk = ink;
        }

        void setNames(String left, String right, String leftNote, String rightNote) {
            this.left = left;
            this.right = right;
            this.leftNote = leftNote;
            this.rightNote = rightNote;
            getAccessibleContext().setAccessibleName(left + ", " + leftNote + ", versus " + right + ", " + rightNote);
            repaint();
        }

        void play(int delayMs, int durationMs) {
            if (Motion.reduced()) return;
            set(0, 0);
            Motion.after(delayMs, () -> Motion.animate(durationMs,
                    p -> set(Math.min(1, p / 0.6), Math.max(0, (p - 0.5) / 0.5)), null));
        }

        private void set(double names, double badge) {
            this.names = names;
            this.badge = badge;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics big = getFontMetrics(Theme.display(NAME_SIZE));
            FontMetrics small = getFontMetrics(Theme.body(Theme.SIZE_S));
            return new Dimension(240, big.getHeight() + small.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setFont(Theme.display(Theme.SIZE_L));
            FontMetrics vsFm = g2.getFontMetrics();
            int badgeSize = vsFm.getHeight() + Theme.SPACE_L;
            int gap = Theme.SPACE_L;
            int side = (getWidth() - badgeSize - 2 * gap) / 2;

            Graphics2D names2 = (Graphics2D) g2.create();
            names2.setComposite(AlphaComposite.SrcOver.derive(clamp01(names)));
            int shift = (int) Math.round((1 - names) * SLIDE_PX);
            drawSide(names2, left, leftNote, -shift, side, false);
            drawSide(names2, right, rightNote, getWidth() - side + shift, side, true);
            names2.dispose();

            // Pop: grows to about 115% and settles at 100%.
            double scale = badge * (1 + 0.15 * Math.sin(Math.PI * badge));
            Graphics2D b = (Graphics2D) g2.create();
            b.translate(getWidth() / 2, getHeight() / 2);
            b.scale(scale, scale);
            b.setColor(badgeFill);
            b.fillOval(-badgeSize / 2, -badgeSize / 2, badgeSize, badgeSize);
            b.setFont(Theme.display(Theme.SIZE_L));
            b.setColor(badgeInk);
            b.drawString("vs", -vsFm.stringWidth("vs") / 2, -vsFm.getHeight() / 2 + vsFm.getAscent());
            b.dispose();
            g2.dispose();
        }

        /** One name (shrinks until it fits its half) with its record line below. */
        private void drawSide(Graphics2D g, String name, String note, int x, int maxWidth, boolean alignRight) {
            float size = NAME_SIZE;
            g.setFont(Theme.display(size));
            while (size > 22 && g.getFontMetrics().stringWidth(name) > maxWidth) {
                size -= 2;
                g.setFont(Theme.display(size));
            }
            FontMetrics fm = g.getFontMetrics();
            FontMetrics noteFm = g.getFontMetrics(Theme.body(Theme.SIZE_S));
            int block = fm.getHeight() + noteFm.getHeight();
            int top = (getHeight() - block) / 2;
            g.setColor(nameColor);
            g.drawString(name, alignRight ? x + maxWidth - fm.stringWidth(name) : x, top + fm.getAscent());
            g.setFont(Theme.body(Theme.SIZE_S));
            g.drawString(note, alignRight ? x + maxWidth - noteFm.stringWidth(note) : x,
                    top + fm.getHeight() + noteFm.getAscent());
        }
    }

    /** Dark pill on the amber hero: "Playing now · M013 · Semifinal", with a red dot while the match is live. */
    static final class TagPill extends JLabel {
        private static final int DOT = 8;
        private boolean live;

        TagPill() {
            setFont(Theme.body(Theme.SIZE_S).deriveFont(java.awt.Font.BOLD));
            setForeground(Theme.INK);
            setOpaque(false);
        }

        void set(String text, boolean live) {
            this.live = live;
            setText(text);
            int left = Theme.SPACE_M + (live ? DOT + Theme.SPACE_S : 0);
            setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_XS + 2, left, Theme.SPACE_XS + 2, Theme.SPACE_M));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(Theme.BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            if (live) {
                g2.setColor(Theme.LIVE);
                g2.fillOval(Theme.SPACE_M, (getHeight() - DOT) / 2, DOT, DOT);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded inner card (radius 16): PANEL_DEEP for a ready match, a dashed LINE outline for one still waiting. */
    static final class InnerCard extends JPanel {
        private static final int RADIUS = 16;
        private static final BasicStroke DASH = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                10f, new float[] {6f, 5f}, 0f);
        private final boolean waiting;

        InnerCard(String top, String main, boolean waiting) {
            super(new BorderLayout(0, Theme.SPACE_XS));
            this.waiting = waiting;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_M, Theme.SPACE_L, Theme.SPACE_M, Theme.SPACE_L));
            JLabel topLabel = new JLabel(top);
            topLabel.setFont(Theme.body(Theme.SIZE_S).deriveFont(java.awt.Font.BOLD));
            topLabel.setForeground(Theme.INK_MUTED);
            JLabel mainLabel = new JLabel(main);
            mainLabel.setFont(waiting ? Theme.body(Theme.SIZE_M) : Theme.display(Theme.SIZE_L));
            mainLabel.setForeground(waiting ? Theme.INK_MUTED : Theme.INK);
            add(topLabel, BorderLayout.NORTH);
            add(mainLabel, BorderLayout.CENTER);
            getAccessibleContext().setAccessibleName(top + ": " + main);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            if (waiting) {
                g2.setColor(Theme.LINE);
                g2.setStroke(DASH);
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 2 * RADIUS, 2 * RADIUS);
            } else {
                g2.setColor(Theme.PANEL_DEEP);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 2 * RADIUS, 2 * RADIUS);
                g2.setColor(Theme.PRIMARY); // ready: amber outline, and "Ready" in the top line
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 2 * RADIUS, 2 * RADIUS);
            }
            g2.dispose();
        }
    }
}
