package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JButton;

/** UI_GUIDE button: 44px pill, sentence-case body font, no shadow, own focus ring. */
public class StyledButton extends JButton {
    /** NORMAL is the secondary button; GHOST is text only (a fill shows on hover) for low-weight actions. */
    public enum Kind { NORMAL, PRIMARY, DANGER, GHOST }

    /** Client property: set to Boolean.TRUE on buttons that sit on an amber tile. */
    public static final String ON_ACCENT = "tbm.onAccent";

    private static final int RING = 3;     // room for the focus ring outside the pill
    private static final int EDGE = 2;     // outline width for Normal and Danger

    private final Kind kind;

    public StyledButton(String text) {
        this(text, Kind.NORMAL);
    }

    public StyledButton(String text, Kind kind) {
        super(text);
        this.kind = kind;
        setFont(Theme.body(Theme.SIZE_M).deriveFont(Font.BOLD));
        setMargin(new Insets(0, 0, 0, 0));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** A left-aligned, transparent row of buttons. Each button already carries its own gap. */
    public static javax.swing.JPanel row(StyledButton... buttons) {
        javax.swing.JPanel row = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        for (StyledButton b : buttons) row.add(b);
        return row;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int padX = Theme.SPACE_L + Theme.SPACE_XS;
        return new Dimension(fm.stringWidth(getText()) + 2 * padX + 2 * RING, Theme.CONTROL_H + 2 * RING);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        boolean down = getModel().isArmed() && getModel().isPressed();
        boolean hover = getModel().isRollover() && isEnabled();
        int x = RING, y = RING;
        int w = getWidth() - 2 * RING;
        int h = Theme.CONTROL_H;

        Color fill;
        Color edge;
        Color text;
        switch (kind) {
            case PRIMARY -> { fill = hover ? Theme.PRIMARY_HOVER : Theme.PRIMARY; edge = null; text = Theme.ON_PRIMARY; }
            case DANGER -> { fill = hover ? Theme.PANEL_DEEP : null; edge = Theme.DANGER_TEXT; text = Theme.DANGER_TEXT; }
            case GHOST -> { fill = hover ? Theme.PANEL_DEEP : null; edge = null; text = Theme.INK; }
            default -> { fill = hover ? Theme.RAISE : Theme.PANEL_DEEP; edge = Theme.LINE; text = Theme.INK; }
        }
        if (down && fill != null) fill = fill.darker();
        if (!isEnabled()) {
            fill = Theme.PANEL_DEEP;
            edge = null;
            text = Theme.INK_MUTED;
        }

        if (fill != null) {
            g2.setColor(fill);
            g2.fillRoundRect(x, y, w, h, h, h);
        }
        if (edge != null) {
            g2.setColor(edge);
            g2.setStroke(new BasicStroke(EDGE));
            g2.drawRoundRect(x + EDGE / 2, y + EDGE / 2, w - EDGE, h - EDGE, h - EDGE, h - EDGE);
        }

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        String label = getText();
        g2.setColor(text);
        g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + (h - fm.getHeight()) / 2 + fm.getAscent());

        if (isFocusOwner()) {
            // On the amber hero tile an amber ring would vanish, so those buttons get a dark ring.
            g2.setColor(Boolean.TRUE.equals(getClientProperty(ON_ACCENT)) ? Theme.ON_PRIMARY : Theme.FOCUS);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(1, 1, getWidth() - 3, h + 2 * RING - 3, h + 2 * RING, h + 2 * RING);
        }
        g2.dispose();
    }
}
