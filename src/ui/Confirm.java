package ui;

import java.awt.Component;
import javax.swing.JOptionPane;

/** Confirmation dialog for Exit, Delete, Undo and Regenerate. Cancel is the default button. */
public final class Confirm {
    private Confirm() {}

    /** Returns true when the user picks the action button. Say in the message what is lost. */
    public static boolean ask(Component parent, String title, String message, String yes) {
        Object[] options = {yes, "Cancel"};
        int choice = JOptionPane.showOptionDialog(parent, message, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[1]);
        return choice == 0;
    }
}
