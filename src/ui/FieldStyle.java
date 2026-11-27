package ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

/** UI_GUIDE inputs: 44px, rounded (12) PANEL_DEEP fill, 1px INK_MUTED outline (3:1), 2px amber outline when focused. */
public final class FieldStyle {
    static final int RADIUS = 12;

    private FieldStyle() {}

    public static void apply(JTextComponent field) {
        // The rounded fill is painted by the UI before the text, so the field itself stays transparent.
        if (field instanceof JPasswordField) {
            field.setUI(new BasicPasswordFieldUI() {
                @Override
                protected void paintSafely(Graphics g) {
                    paintFill(g, field);
                    super.paintSafely(g);
                }
            });
        } else {
            field.setUI(new BasicTextFieldUI() {
                @Override
                protected void paintSafely(Graphics g) {
                    paintFill(g, field);
                    super.paintSafely(g);
                }
            });
        }
        field.setOpaque(false);
        field.setFont(Theme.body(Theme.SIZE_M));
        field.setForeground(Theme.INK);
        field.setCaretColor(Theme.INK);
        field.setBackground(Theme.PANEL_DEEP);
        field.setSelectionColor(Theme.RAISE);
        field.setSelectedTextColor(Theme.INK);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, Theme.CONTROL_H));
        field.setBorder(new Round(false));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(new Round(true));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(new Round(false));
            }
        });
    }

    private static final String ERROR = "tbm.fieldError";

    /** Marks a field invalid: a 2px DANGER_TEXT outline until cleared. Put the message under the field. */
    public static void setError(JComponent field, boolean error) {
        field.putClientProperty(ERROR, error ? Boolean.TRUE : null);
        field.repaint();
    }

    /** Label above the field. The label is linked to the field for screen readers. */
    public static JPanel labeled(String text, JComponent field) {
        return labeled(new JLabel(text), field);
    }

    /** Same, with a label the caller keeps so it can change the text later. */
    public static JPanel labeled(JLabel label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, Theme.SPACE_XS));
        p.setOpaque(false);
        label.setFont(Theme.body(Theme.SIZE_S).deriveFont(Font.BOLD));
        label.setForeground(Theme.INK_MUTED);
        label.setLabelFor(field);
        p.add(label, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    /** Rounded background in the component's background color. Shared with ComboStyle. */
    static void paintFill(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getBackground());
        g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 2 * RADIUS, 2 * RADIUS);
        g2.dispose();
    }

    /** Rounded outline: 1px INK_MUTED, or 2px FOCUS when focused. Shared with ComboStyle. */
    static final class Round implements Border {
        private final boolean focused;

        Round(boolean focused) {
            this.focused = focused;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean error = c instanceof JComponent jc && Boolean.TRUE.equals(jc.getClientProperty(ERROR));
            int stroke = focused || error ? 2 : 1;
            // INK_MUTED, not LINE: an input's outline needs 3:1 against its surroundings (LINE is about 1.4:1).
            g2.setColor(error ? Theme.DANGER_TEXT : focused ? Theme.FOCUS : Theme.INK_MUTED);
            g2.setStroke(new BasicStroke(stroke));
            float o = stroke / 2f;
            g2.draw(new RoundRectangle2D.Float(x + o, y + o, w - stroke, h - stroke,
                    2 * RADIUS - stroke, 2 * RADIUS - stroke));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, Theme.SPACE_L, 2, Theme.SPACE_L);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
