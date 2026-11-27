package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * The app's own modal dialog, drawn in the theme instead of the operating system's: a rounded PANEL tile with a
 * 1px LINE outline, a round icon badge, a display-font title, the message, then pill buttons. No title bar.
 * Esc cancels. Enter presses the focused button. Used for Exit, Delete, Undo, Generate and "Who won?".
 */
public final class Confirm {
    private static final int WIDTH = 460;
    private static final int RADIUS = 24;
    private static final int PAD = 28;
    private static final int BADGE = 48;
    private static final int GLYPH = 24;
    private static final int RING = 3;      // StyledButton keeps a 3px margin for its focus ring
    private static final String CANCEL = "Cancel";

    private Confirm() {}

    /** A destructive confirmation (red icon, red action button). Cancel has focus. Say in the message what is lost. */
    public static boolean ask(Component parent, String title, String message, String yes) {
        return ask(parent, title, message, yes, true);
    }

    /** danger=false gives an amber icon and an amber action button, for actions that lose nothing. */
    public static boolean ask(Component parent, String title, String message, String yes, boolean danger) {
        return show(parent, title, message, danger, new String[] {yes}, false) == 0;
    }

    /** One button per choice, stacked, and Cancel under them. Returns the index picked, or -1 for Cancel or Esc. */
    public static int choose(Component parent, String title, String message, String... choices) {
        return show(parent, title, message, false, choices, true);
    }

    private static int show(Component parent, String title, String message, boolean danger, String[] choices,
            boolean stacked) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setResizable(false);
        try {
            dialog.setBackground(Theme.CLEAR);
        } catch (UnsupportedOperationException noTranslucency) {
            dialog.getContentPane().setBackground(Theme.PANEL); // square corners, still on-theme
        }
        int[] picked = {-1};

        StyledButton cancel = new StyledButton(CANCEL, stacked ? StyledButton.Kind.GHOST : StyledButton.Kind.NORMAL);
        cancel.addActionListener(e -> dialog.dispose());
        StyledButton[] buttons = new StyledButton[choices.length];
        for (int i = 0; i < choices.length; i++) {
            int index = i;
            StyledButton.Kind kind = stacked ? StyledButton.Kind.NORMAL
                    : danger ? StyledButton.Kind.DANGER : StyledButton.Kind.PRIMARY;
            buttons[i] = new StyledButton(choices[i], kind);
            buttons[i].addActionListener(e -> {
                picked[0] = index;
                dialog.dispose();
            });
        }

        JPanel actions;
        if (stacked) {
            actions = new JPanel(new GridLayout(0, 1, 0, 0));
            for (StyledButton b : buttons) actions.add(b);
            actions.add(cancel);
        } else {
            actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            actions.add(cancel);
            for (StyledButton b : buttons) actions.add(b);
        }
        actions.setOpaque(false);

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.display(Theme.SIZE_L));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        int textWidth = WIDTH - 2 * PAD - BADGE - Theme.SPACE_L;
        JTextArea body = new JTextArea(message); // a text area wraps at a width we set, so nothing is clipped
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setEditable(false);
        body.setFocusable(false);
        body.setOpaque(false);
        body.setBorder(null);
        body.setFont(Theme.body(Theme.SIZE_M));
        body.setForeground(Theme.INK);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.setSize(textWidth, Short.MAX_VALUE);
        body.setPreferredSize(new Dimension(textWidth, body.getPreferredSize().height));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(heading);
        text.add(Box.createVerticalStrut(Theme.SPACE_S));
        text.add(body);

        JPanel badgeSlot = new JPanel(new BorderLayout());
        badgeSlot.setOpaque(false);
        badgeSlot.add(new Badge(danger), BorderLayout.NORTH);
        JPanel head = new JPanel(new BorderLayout(Theme.SPACE_L, 0));
        head.setOpaque(false);
        head.add(badgeSlot, BorderLayout.WEST);
        head.add(text, BorderLayout.CENTER);

        JPanel card = new JPanel(new BorderLayout(0, Theme.SPACE_XL)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 2 * RADIUS, 2 * RADIUS);
                g2.setColor(Theme.LINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 2 * RADIUS, 2 * RADIUS);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD - RING, PAD - RING));
        card.add(head, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        card.setPreferredSize(new Dimension(WIDTH, card.getPreferredSize().height));
        dialog.setContentPane(card);
        dialog.getAccessibleContext().setAccessibleName(title);
        dialog.getAccessibleContext().setAccessibleDescription(message);

        JComponent keys = dialog.getRootPane();
        keys.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        keys.getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        keys.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "press");
        keys.getActionMap().put("press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof AbstractButton b) {
                    b.doClick();
                }
            }
        });

        // A warning starts on Cancel so a stray Enter loses nothing; a picker starts on its first choice.
        StyledButton first = stacked ? buttons[0] : cancel;
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                first.requestFocusInWindow();
            }
        });
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true); // blocks until a button, Esc or dispose
        return picked[0];
    }

    /** Round icon badge: a red-tinted alert for a warning, an amber-tinted trophy for a choice or a safe action. */
    private static final class Badge extends JComponent {
        private final boolean danger;

        Badge(boolean danger) {
            this.danger = danger;
            setPreferredSize(new Dimension(BADGE, BADGE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(danger ? Theme.LIVE_TINT : Theme.PLAYED_TINT);
            g2.fillOval(0, 0, BADGE, BADGE);
            Glyphs.paint(g2, danger ? Glyphs.Kind.ALERT : Glyphs.Kind.TROPHY, (BADGE - GLYPH) / 2, (BADGE - GLYPH) / 2,
                    GLYPH, danger ? Theme.DANGER_TEXT : Theme.PRIMARY);
            g2.dispose();
        }
    }
}
