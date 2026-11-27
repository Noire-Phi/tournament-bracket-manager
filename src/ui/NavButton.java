package ui;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/** One item in the header's pill bar. Active is an amber pill with ON_PRIMARY text, hover is PANEL_DEEP. */
public class NavButton extends JButton {
    private static final int PAD_X = 18;
    private boolean active;

    public NavButton(String text) {
        super(text);
        setFont(Theme.body(Theme.SIZE_M));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setActive(boolean active) {
        this.active = active;
        getAccessibleContext().setAccessibleDescription(active ? "Current page" : null);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        // Sized for the bold label, so the pill does not jump when it becomes active.
        FontMetrics fm = getFontMetrics(getFont().deriveFont(Font.BOLD));
        return new Dimension(fm.stringWidth(getText()) + 2 * PAD_X, Theme.CONTROL_H);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        boolean hover = getModel().isRollover();
        if (active || hover) {
            g2.setColor(active ? Theme.PRIMARY : Theme.PANEL_DEEP);
            g2.fillRoundRect(0, 0, w, h, h, h);
        }
        g2.setFont(getFont().deriveFont(active ? Font.BOLD : Font.PLAIN));
        g2.setColor(active ? Theme.ON_PRIMARY : Theme.INK);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
        if (isFocusOwner()) {
            // On the amber pill an amber ring would vanish, so the active item gets a dark ring instead.
            g2.setColor(active ? Theme.ON_PRIMARY : Theme.FOCUS);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(1, 1, w - 3, h - 3, h - 2, h - 2);
        }
        g2.dispose();
    }
}
