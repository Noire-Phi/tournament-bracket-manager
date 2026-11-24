package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

/** UI_GUIDE inputs: 44px, PANEL_DEEP fill, 2px LINE border that turns FOCUS when focused, visible label above. */
public final class FieldStyle {
    private FieldStyle() {}

    public static void apply(JTextComponent field) {
        field.setFont(Theme.body(14));
        field.setForeground(Theme.INK);
        field.setCaretColor(Theme.INK);
        field.setBackground(Theme.PANEL_DEEP);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, Theme.CONTROL_H));
        border(field, false);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                border(field, true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                border(field, false);
            }
        });
    }

    /** Label above the field. The label is linked to the field for screen readers. */
    public static JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, Theme.SPACE_XS));
        p.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setFont(Theme.body(13));
        label.setForeground(Theme.INK_MUTED);
        label.setLabelFor(field);
        p.add(label, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private static void border(JComponent c, boolean focused) {
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(Theme.BORDER, Theme.BORDER, Theme.BORDER, Theme.BORDER,
                        focused ? Theme.FOCUS : Theme.LINE),
                BorderFactory.createEmptyBorder(0, Theme.SPACE_M, 0, Theme.SPACE_M)));
    }
}
