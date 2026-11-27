package ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

/** Keeps the focused control on screen: when Tab lands below the fold, every enclosing scroll pane scrolls to it. */
final class FocusReveal {
    private static final int MAX_SLICE = 2 * Theme.CONTROL_H; // for a big component, show its top, not all of it

    private FocusReveal() {}

    /** Installs the listener for one window. Call once per window. */
    static void install(Container window) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", e -> {
            if (e.getNewValue() instanceof JComponent c && SwingUtilities.isDescendingFrom(c, window)) reveal(c);
        });
    }

    static void reveal(JComponent c) {
        Rectangle own = new Rectangle(0, 0, c.getWidth(), Math.min(c.getHeight(), MAX_SLICE));
        // Inner scroll panes first, then the page's: JViewport does not pass the request on by itself.
        for (Component p = c.getParent(); p != null; p = p.getParent()) {
            if (p instanceof JViewport vp && vp.getView() instanceof JComponent view) {
                view.scrollRectToVisible(SwingUtilities.convertRectangle(c, own, view));
            }
        }
    }
}
