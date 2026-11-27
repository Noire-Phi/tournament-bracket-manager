package ui;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Standings rank chips (UI_GUIDE section 4): an amber chip for the leaders, a LINE chip for the next group,
 * and a plain muted number for the rest.
 */
final class RankChip {
    static final int SIZE = 32;
    static final int LEADERS = algo.Ranks.LEADERS;
    static final int NEXT = algo.Ranks.NEXT;
    static final int REST = algo.Ranks.REST;
    private static final int ARC = 10;

    private RankChip() {}

    /** Paints one chip with its top-left corner at x, y. */
    static void paint(Graphics2D g2, int x, int y, int rank, int group) {
        String text = String.valueOf(rank);
        g2.setFont(group == REST ? Theme.body(Theme.SIZE_S) : Theme.body(Theme.SIZE_S).deriveFont(java.awt.Font.BOLD));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (SIZE - fm.stringWidth(text)) / 2;
        int ty = y + (SIZE - fm.getHeight()) / 2 + fm.getAscent();
        if (group == LEADERS) {
            g2.setColor(Theme.PRIMARY);
            g2.fillRoundRect(x, y, SIZE, SIZE, ARC, ARC);
            g2.setColor(Theme.ON_PRIMARY);
        } else if (group == NEXT) {
            g2.setColor(Theme.LINE);
            g2.fillRoundRect(x, y, SIZE, SIZE, ARC, ARC);
            g2.setColor(Theme.INK);
        } else {
            g2.setColor(Theme.INK_MUTED);
        }
        g2.drawString(text, tx, ty);
    }
}
