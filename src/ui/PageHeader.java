package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Page title on the left with a one-line muted caption. Without actions the caption sits at the right edge;
 * with actions (buttons on the right) it follows the title on the same baseline. Long captions end in "...",
 * they never run under the title or the buttons.
 */
final class PageHeader extends JPanel {
    private final JLabel title = new JLabel();
    private final JLabel caption = new JLabel();
    private final JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_L, 0));

    PageHeader() {
        super(new BorderLayout(Theme.SPACE_XL, 0));
        setOpaque(false);
        title.setFont(Theme.display(Theme.SIZE_XXL));
        title.setForeground(Theme.INK);
        caption.setFont(Theme.body(Theme.SIZE_M));
        caption.setForeground(Theme.INK_MUTED);
        caption.setHorizontalAlignment(SwingConstants.RIGHT);
        caption.setVerticalAlignment(SwingConstants.BOTTOM);
        caption.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, Theme.SPACE_S, 0));
        ((FlowLayout) titleRow.getLayout()).setAlignOnBaseline(true);
        titleRow.setOpaque(false);
        titleRow.add(title);
        add(titleRow, BorderLayout.WEST);
        add(caption, BorderLayout.CENTER);
    }

    /** Buttons on the right; the caption moves next to the title. */
    void setActions(JComponent actions) {
        remove(caption);
        caption.setBorder(null);
        caption.setHorizontalAlignment(SwingConstants.LEFT);
        titleRow.add(caption);
        add(actions, BorderLayout.EAST);
    }

    void set(String titleText, String captionText) {
        title.setText(titleText);
        caption.setText(captionText);
        getAccessibleContext().setAccessibleName(titleText);
    }
}
