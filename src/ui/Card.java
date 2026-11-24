package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** UI_GUIDE card: PANEL fill, 2px LINE border, 18px padding, PRIMARY slash before the title. */
public class Card extends JPanel {
    private final JLabel title = new JLabel();
    private final JPanel body = new JPanel(new BorderLayout());

    public Card(String titleText) {
        super(new BorderLayout(0, Theme.SPACE_M));
        setBackground(Theme.PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(Theme.BORDER, Theme.BORDER, Theme.BORDER, Theme.BORDER, Theme.LINE),
                BorderFactory.createEmptyBorder(Theme.CARD_PAD, Theme.CARD_PAD, Theme.CARD_PAD, Theme.CARD_PAD)));

        title.setFont(Theme.display(20));
        title.setForeground(Theme.INK);
        title.setIcon(new Slash());
        title.setIconTextGap(Theme.SPACE_M - Theme.SPACE_XS);
        body.setOpaque(false);
        setTitle(titleText);

        add(title, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public void setTitle(String text) {
        title.setText(text.toUpperCase());
        getAccessibleContext().setAccessibleName(text);
    }

    /** Put the card's content here. */
    public JPanel body() {
        return body;
    }

    /** 10 x 20 slanted bar (skewX -20 degrees). */
    private static final class Slash implements Icon {
        private static final int W = 10, H = 20, SKEW = 7;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PRIMARY);
            g2.fill(new Polygon(
                    new int[] {x + SKEW, x + SKEW + W, x + W, x},
                    new int[] {y, y, y + H, y + H}, 4));
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return W + SKEW;
        }

        @Override
        public int getIconHeight() {
            return H;
        }
    }
}
