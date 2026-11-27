package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;
import javax.swing.JComponent;

/** Line glyphs drawn with Java2D on a 24-unit grid, so they stay sharp at any size. No image files. */
final class Glyphs {
    enum Kind { TROPHY, CHECK, ALERT, PENCIL }

    private Glyphs() {}

    static void paint(Graphics2D g, Kind kind, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(x, y);
        g2.scale(size / 24.0, size / 24.0);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (kind) {
            case TROPHY -> {
                Path2D cup = new Path2D.Double();
                cup.moveTo(7, 4);
                cup.lineTo(17, 4);
                cup.lineTo(17, 9);
                cup.curveTo(17, 12.5, 14.8, 15, 12, 15);
                cup.curveTo(9.2, 15, 7, 12.5, 7, 9);
                cup.closePath();
                g2.draw(cup);
                Path2D handles = new Path2D.Double();
                handles.moveTo(7, 6);
                handles.lineTo(4, 6);
                handles.curveTo(4, 9.5, 5.5, 11, 7.6, 11.4);
                handles.moveTo(17, 6);
                handles.lineTo(20, 6);
                handles.curveTo(20, 9.5, 18.5, 11, 16.4, 11.4);
                g2.draw(handles);
                g2.drawLine(12, 15, 12, 18);
                g2.drawLine(8, 20, 16, 20);
                g2.drawLine(9, 18, 15, 18);
            }
            case CHECK -> {
                Path2D p = new Path2D.Double();
                p.moveTo(5, 12.5);
                p.lineTo(10, 17.5);
                p.lineTo(19, 7);
                g2.draw(p);
            }
            case ALERT -> {
                g2.drawLine(12, 5, 12, 14);
                g2.fillOval(11, 17, 2, 2);
            }
            case PENCIL -> {
                Path2D p = new Path2D.Double();
                p.moveTo(15.5, 5.5);
                p.lineTo(18.5, 8.5);
                p.lineTo(9, 18);
                p.lineTo(5, 19);
                p.lineTo(6, 15);
                p.closePath();
                g2.draw(p);
            }
        }
        g2.dispose();
    }

    /** A glyph as an Icon, for labels and buttons. */
    static Icon icon(Kind kind, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                paint((Graphics2D) g, kind, x, y, size, color);
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /** The brand mark: an amber rounded square with the trophy in ON_PRIMARY. */
    static final class Logo extends JComponent {
        private final int size;

        Logo(int size) {
            this.size = size;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(size, size);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PRIMARY);
            g2.fillRoundRect(0, 0, size, size, size / 2, size / 2);
            int glyph = size * 6 / 10;
            Glyphs.paint(g2, Kind.TROPHY, (size - glyph) / 2, (size - glyph) / 2, glyph, Theme.ON_PRIMARY);
            g2.dispose();
        }
    }
}
