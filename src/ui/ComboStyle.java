package ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/** Drop-down in the same look as FieldStyle: 44px, rounded PANEL_DEEP fill, outline that turns FOCUS when focused. */
public final class ComboStyle {
    private static final int ARROW_W = 40;

    private ComboStyle() {}

    public static void apply(JComboBox<?> combo) {
        combo.setUI(new BasicComboBoxUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                FieldStyle.paintFill(g, c);   // rounded fill under the value and the arrow
                super.paint(g, c);
            }

            @Override
            protected JButton createArrowButton() {
                return new Arrow();
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(Theme.PANEL_DEEP);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = (BasicComboPopup) super.createPopup();
                popup.setBorder(BorderFactory.createMatteBorder(Theme.BORDER, Theme.BORDER, Theme.BORDER, Theme.BORDER, Theme.LINE));
                popup.getList().setBackground(Theme.PANEL_DEEP);
                popup.getList().setForeground(Theme.INK);
                popup.getList().setSelectionBackground(Theme.RAISE);
                popup.getList().setSelectionForeground(Theme.INK);
                return popup;
            }
        });
        combo.setOpaque(false);
        combo.setFont(Theme.body(Theme.SIZE_M));
        combo.setForeground(Theme.INK);
        combo.setBackground(Theme.PANEL_DEEP);
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, Theme.CONTROL_H));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, false, false);
                setFont(Theme.body(Theme.SIZE_S));
                setForeground(Theme.INK);
                setBackground(selected ? Theme.RAISE : Theme.PANEL_DEEP);
                setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_S, Theme.SPACE_M, Theme.SPACE_S, Theme.SPACE_M));
                return this;
            }
        });
        border(combo, false);
        combo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                border(combo, true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                border(combo, false);
            }
        });
    }

    private static void border(JComponent c, boolean focused) {
        c.setBorder(new FieldStyle.Round(focused));
    }

    /** Down arrow, painted as a small triangle. */
    private static final class Arrow extends JButton {
        Arrow() {
            setBorder(BorderFactory.createEmptyBorder());
            setContentAreaFilled(false);
            setFocusable(false);
        }

        /** The combo box itself takes focus; the arrow is not a second, unnamed tab stop. */
        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(ARROW_W, Theme.CONTROL_H);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PANEL_DEEP);
            g2.fillRect(0, 0, getWidth(), getHeight());
            int cx = getWidth() / 2, cy = getHeight() / 2;
            g2.setColor(Theme.INK);
            g2.fill(new Polygon(new int[] {cx - 6, cx + 6, cx}, new int[] {cy - 3, cy - 3, cy + 4}, 3));
            g2.dispose();
        }
    }
}
