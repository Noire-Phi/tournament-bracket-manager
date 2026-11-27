package ui;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import model.SportMode;
import model.Tournament;

/** Segmented pill switch for the sport mode. The selected value is shown in text and marked in the accessible name. */
public class SportSwitch extends JPanel {
    private static final int INSET = 3;

    private final Tournament tournament;
    private final Map<SportMode, Seg> segs = new EnumMap<>(SportMode.class);

    /** onPick runs after the tournament's mode changed. */
    public SportSwitch(Tournament tournament, Consumer<SportMode> onPick) {
        super(new GridLayout(1, 0, 2, 0));
        this.tournament = tournament;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(INSET, INSET, INSET, INSET));
        getAccessibleContext().setAccessibleName("Sport mode");
        ButtonGroup group = new ButtonGroup();
        for (SportMode mode : SportMode.values()) {
            Seg seg = new Seg(mode.shortLabel);
            seg.addActionListener(e -> {
                if (tournament.sport != mode) {
                    tournament.sport = mode;
                    onPick.accept(mode);
                }
            });
            group.add(seg);
            add(seg);
            segs.put(mode, seg);
        }
        sync();
    }

    /** Re-reads the tournament's mode. Call after the mode changed elsewhere. */
    public final void sync() {
        segs.forEach((mode, seg) -> {
            seg.setSelected(mode == tournament.sport);
            seg.getAccessibleContext().setAccessibleName(mode.label + (mode == tournament.sport ? " (selected)" : ""));
        });
        repaint();
    }

    /** The outer pill: PANEL fill with a LINE outline. */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int h = getHeight();
        g2.setColor(Theme.PANEL);
        g2.fillRoundRect(0, 0, getWidth(), h, h, h);
        g2.setColor(Theme.LINE);
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(0, 0, getWidth() - 1, h - 1, h, h);
        g2.dispose();
    }

    private static final class Seg extends JToggleButton {
        Seg(String text) {
            super(text);
            setFont(Theme.body(Theme.SIZE_S));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont().deriveFont(Font.BOLD));
            return new Dimension(fm.stringWidth(getText()) + 2 * Theme.SPACE_L, Theme.CONTROL_H - 2 * INSET);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (isSelected() || getModel().isRollover()) {
                g2.setColor(isSelected() ? Theme.PANEL_DEEP : Theme.RAISE);
                g2.fillRoundRect(0, 0, w, h, h, h);
            }
            g2.setFont(getFont().deriveFont(isSelected() ? Font.BOLD : Font.PLAIN));
            g2.setColor(isSelected() ? Theme.INK : Theme.INK_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            String label = getText();
            g2.drawString(label, (w - fm.stringWidth(label)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            if (isFocusOwner()) {
                g2.setColor(Theme.FOCUS);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, w - 3, h - 3, h - 2, h - 2);
            }
            g2.dispose();
        }
    }
}
