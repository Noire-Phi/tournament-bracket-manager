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
            new LoginFrame(() -> new MainFrame(tournament).setVisible(true)).setVisible(true);
        });
    }

    /** Makes JOptionPane confirmations match the theme. */
    private static void styleDialogs() {
        UIManager.put("OptionPane.background", Theme.PANEL);
        UIManager.put("Panel.background", Theme.PANEL);
        UIManager.put("OptionPane.messageForeground", Theme.INK);
        UIManager.put("OptionPane.messageFont", Theme.body(14));
        UIManager.put("OptionPane.buttonFont", Theme.display(17));
        UIManager.put("Button.background", Theme.RAISE);
        UIManager.put("Button.foreground", Theme.INK);
        UIManager.put("Button.font", Theme.display(17));
    }
}
