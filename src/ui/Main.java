package ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import model.Tournament;

/** Entry point. The login page comes first; the main window opens after it. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            styleDialogs();
            Tournament tournament = new Tournament("New Tournament");
            new LoginFrame(tournament, () -> new MainFrame(tournament).setVisible(true)).setVisible(true);
        });
    }

    /** Makes scrollbars match the theme (dialogs are drawn by Confirm). Run before any window is built. */
    static void styleDialogs() {
        // Dark scrollbars (Metal look and feel keys): RAISE thumb on a BG track.
        for (String k : new String[] {"ScrollBar.background", "ScrollBar.track", "ScrollBar.trackHighlight",
                "ScrollBar.highlight", "ScrollBar.shadow", "ScrollBar.darkShadow"}) {
            UIManager.put(k, Theme.BG);
        }
        for (String k : new String[] {"ScrollBar.thumb", "ScrollBar.thumbHighlight", "ScrollBar.thumbShadow",
                "ScrollBar.thumbDarkShadow"}) {
            UIManager.put(k, Theme.RAISE);
        }
        UIManager.put("ScrollBar.width", 10);
        // Metal's Ocean theme paints a light gradient over the thumb; a flat RAISE "gradient" keeps it dark.
        UIManager.put("ScrollBar.gradient", java.util.List.of(0.3f, 0f, Theme.RAISE, Theme.RAISE, Theme.RAISE));
    }
}
