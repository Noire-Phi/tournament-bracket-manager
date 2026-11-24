package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JButton;

/** UI_GUIDE button: 44px body, display font caps, hard shadow, own 2px focus ring. */
public class StyledButton extends JButton {
    public enum Kind { NORMAL, PRIMARY, DANGER }

    private static final int SHADOW = 3;
    private static final int PRESSED_SHADOW = 1;
    private static final int PRESS_MOVE = 2;
    private static final int PAD = 2;      // room for the focus ring
    private static final int ARC = 6;      // 3px radius

    private final Kind kind;

    public StyledButton(String text) {
        this(text, Kind.NORMAL);
    }

    public StyledButton(String text, Kind kind) {
        super(text);
        this.kind = kind;
        setFont(Theme.display(19));
        setMargin(new Insets(0, 0, 0, 0));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int textW = fm.stringWidth(getText().toUpperCase());
        int padX = Theme.SPACE_L + Theme.SPACE_XS;
        return new Dimension(textW + 2 * padX + SHADOW + 2 * PAD, Theme.CONTROL_H + SHADOW + 2 * PAD);
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
        int move = down ? PRESS_MOVE : 0;
        int shadow = down ? PRESSED_SHADOW : SHADOW;
        int x = PAD + move, y = PAD + move;
        int w = getWidth() - SHADOW - 2 * PAD;
        int h = Theme.CONTROL_H;

        Color shadowColor = kind == Kind.DANGER ? Theme.DANGER_TEXT : Theme.INK;
        Color edge = kind == Kind.PRIMARY ? Theme.PRIMARY : shadowColor;
        Color fill;
        Color text;
        switch (kind) {
            case PRIMARY -> { fill = hover ? Theme.PRIMARY_HOVER : Theme.PRIMARY; text = Theme.ON_PRIMARY; }
            case DANGER -> { fill = hover ? Theme.RAISE : Theme.PANEL_DEEP; text = Theme.DANGER_TEXT; }
            default -> { fill = hover ? Theme.RAISE : Theme.PANEL_DEEP; text = Theme.INK; }
        }
        if (!isEnabled()) {
            shadowColor = Theme.LINE;
            edge = Theme.LINE;
            fill = Theme.PANEL_DEEP;
            text = Theme.INK_MUTED;
        }

        g2.setColor(shadowColor);
        g2.fillRoundRect(x + shadow, y + shadow, w, h, ARC, ARC);
        g2.setColor(fill);
        g2.fillRoundRect(x, y, w, h, ARC, ARC);
        g2.setColor(edge);
        g2.setStroke(new BasicStroke(Theme.BORDER));
        g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, ARC, ARC);

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        String label = getText().toUpperCase();
        g2.setColor(text);
        g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + (h - fm.getHeight()) / 2 + fm.getAscent());

        if (isFocusOwner()) {
            g2.setColor(Theme.FOCUS);
            g2.setStroke(new BasicStroke(Theme.BORDER));
            g2.drawRoundRect(x - 1, y - 1, w + 2, h + 2, ARC + 2, ARC + 2);
        }
        g2.dispose();
    }
}
