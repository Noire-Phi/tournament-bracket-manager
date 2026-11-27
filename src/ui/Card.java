package ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** UI_GUIDE tile: rounded (24) PANEL fill, 1px LINE outline, 24px padding, sentence-case title. */
public class Card extends JPanel {
    private static final int RADIUS = 24;

    private final JLabel title = new JLabel();
    private final JPanel body = new JPanel(new BorderLayout());

    public Card(String titleText) {
        super(new BorderLayout(0, Theme.SPACE_M));
        setOpaque(false);
        setBackground(Theme.PANEL);
        setBorder(BorderFactory.createEmptyBorder(Theme.CARD_PAD, Theme.CARD_PAD, Theme.CARD_PAD, Theme.CARD_PAD));

        title.setFont(Theme.display(Theme.SIZE_L));
        title.setForeground(Theme.INK);
        body.setOpaque(false);
        setTitle(titleText);

        add(title, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public void setTitle(String text) {
        title.setText(text);
        title.setVisible(!text.isEmpty());   // a tile with no title just holds its content
        getAccessibleContext().setAccessibleName(text);
    }

    /** For the amber hero tile: the title switches to ON_PRIMARY. */
    public void setTitleColor(java.awt.Color c) {
        title.setForeground(c);
    }

    /** Put the card's content here. */
    public JPanel body() {
        return body;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 2 * RADIUS, 2 * RADIUS);
        if (Theme.PANEL.equals(getBackground())) {   // amber tiles have no outline
            g2.setColor(Theme.LINE);
            g2.setStroke(new BasicStroke(Theme.BORDER));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 2 * RADIUS, 2 * RADIUS);
        }
        g2.dispose();
    }
}
