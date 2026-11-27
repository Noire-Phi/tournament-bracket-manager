package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import model.Match;

/**
 * Table cell renderer that paints a match status as a pill. The word is always shown,
 * so state is never carried by color alone. Register it for Match.Status.class.
 */
public class StatusPill extends JLabel implements TableCellRenderer {
    private static final int PAD_Y_PILL = 3;
    private static final int PAD_X = 10;

    private Match.Status status;
    private boolean selected, first;

    public StatusPill() {
        setOpaque(false);
        setFont(Theme.body(Theme.SIZE_S).deriveFont(java.awt.Font.BOLD));
    }

    public static String label(Match.Status s) {
        return switch (s) {
            case PLAYED -> "Played";
            case READY -> "Ready";
            case IN_PROGRESS -> "In progress";
            case WAITING -> "Waiting";
        };
    }

    private static Color fill(Match.Status s) {
        return switch (s) {
            case PLAYED -> Theme.PLAYED_TINT;
            case READY -> Theme.READY_FILL;
            case IN_PROGRESS -> Theme.LIVE_TINT;
            case WAITING -> Theme.RAISE;
        };
    }

    private static Color text(Match.Status s) {
        return switch (s) {
            case PLAYED -> Theme.WIN;
            case READY -> Theme.ON_PRIMARY;
            case IN_PROGRESS -> Theme.LIVE;
            case WAITING -> Theme.INK_MUTED;
        };
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        status = value instanceof Match.Status s ? s : null;
        selected = isSelected;
        first = column == 0;
        setText(status == null ? "" : label(status));
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        TableStyle.paintRow(g, getWidth(), getHeight(), selected, first);
        if (status == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        String word = getText();
        int w = fm.stringWidth(word) + 2 * PAD_X;
        int h = fm.getHeight() + 2 * PAD_Y_PILL;
        int x = Theme.SPACE_M;
        int y = (getHeight() - h) / 2;
        g2.setColor(fill(status));
        g2.fillRoundRect(x, y, w, h, h, h);
        g2.setColor(text(status));
        g2.drawString(word, x + PAD_X, y + PAD_Y_PILL + fm.getAscent());
        g2.dispose();
    }
}
