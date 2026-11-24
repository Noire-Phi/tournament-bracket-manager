package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/** Nav item: display font caps. The active item has a PRIMARY fill and ON_PRIMARY text. */
public class NavButton extends JButton {
    private boolean active;

    public NavButton(String text) {
        super(text);
        setFont(Theme.display(20));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setHorizontalAlignment(LEFT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, Theme.CONTROL_H);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Theme.CONTROL_H);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        boolean hover = getModel().isRollover();
        if (active) {
            g2.setColor(Theme.PRIMARY);
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else if (hover) {
            g2.setColor(Theme.RAISE);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        Color text = active ? Theme.ON_PRIMARY : Theme.INK;
        g2.setFont(getFont());
        g2.setColor(text);
        FontMetrics fm = g2.getFontMetrics();
        int x = Theme.SPACE_L + (hover && !active ? 3 : 0);
        g2.drawString(getText().toUpperCase(), x, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        if (isFocusOwner()) {
            g2.setColor(Theme.FOCUS);
            g2.setStroke(new BasicStroke(Theme.BORDER));
            g2.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
        }
        g2.dispose();
    }
}
