package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** First screen. One fixed username and password; the main window opens only after a match. */
public class LoginFrame extends JFrame {
    // shortcut: one fixed login kept in code, fine for coursework, swap for stored accounts if real users are needed
    static final String USERNAME = "organizer";
    static final String PASSWORD = "bracket104";
    private static final int CARD_W = 340;
    private static final int SHADOW = 8;

    private final JTextField user = new JTextField();
    private final JPasswordField pass = new JPasswordField();
    private final JLabel error = new JLabel(" ");
    private final Runnable onLogin;

    public LoginFrame(Runnable onLogin) {
        super("Tournament Bracket Manager - Sign in");
        this.onLogin = onLogin;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BG);
        setLayout(new GridBagLayout());

        FieldStyle.apply(user);
        FieldStyle.apply(pass);
        user.getAccessibleContext().setAccessibleName("Username");
        pass.getAccessibleContext().setAccessibleName("Password");

        JLabel eyebrow = new JLabel("TOURNAMENT BRACKET MANAGER");
        eyebrow.setFont(Theme.display(17));
        eyebrow.setForeground(Theme.INK_MUTED);
        JLabel title = new JLabel("SIGN IN");
        title.setFont(Theme.display(48));
        title.setForeground(Theme.INK);

        error.setFont(Theme.body(13));
        error.setForeground(Theme.DANGER_TEXT);

        StyledButton login = new StyledButton("Login", StyledButton.Kind.PRIMARY);
        StyledButton exit = new StyledButton("Exit");
        login.addActionListener(e -> submit());
        exit.addActionListener(e -> {
            if (Confirm.ask(this, "Exit", "Exit the application?", "Exit")) System.exit(0);
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setOpaque(false);
        buttons.add(login);
        buttons.add(exit);
        getRootPane().setDefaultButton(login);

        JLabel hint = new JLabel("Demo login: " + USERNAME + " / " + PASSWORD);
        hint.setFont(Theme.body(13));
        hint.setForeground(Theme.INK_MUTED);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(Theme.PANEL);
        inner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(Theme.BORDER, Theme.BORDER, Theme.BORDER, Theme.BORDER, Theme.INK),
                BorderFactory.createEmptyBorder(26, 26, 26, 26)));
        Box.Filler width = new Box.Filler(new Dimension(CARD_W, 0), new Dimension(CARD_W, 0), new Dimension(CARD_W, 0));
        inner.add(left(width));
        inner.add(left(eyebrow));
        inner.add(left(title));
        inner.add(Box.createVerticalStrut(Theme.SPACE_L));
        inner.add(left(FieldStyle.labeled("Username", user)));
        inner.add(Box.createVerticalStrut(Theme.SPACE_M));
        inner.add(left(FieldStyle.labeled("Password", pass)));
        inner.add(Box.createVerticalStrut(Theme.SPACE_S));
        inner.add(left(error));
        inner.add(Box.createVerticalStrut(Theme.SPACE_S));
        inner.add(left(buttons));
        inner.add(Box.createVerticalStrut(Theme.SPACE_M));
        inner.add(left(hint));

        // Outer panel paints the 8px PRIMARY hard shadow behind the card.
        JPanel outer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Theme.PRIMARY);
                g.fillRect(SHADOW, SHADOW, getWidth() - SHADOW, getHeight() - SHADOW);
            }
        };
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(0, 0, SHADOW, SHADOW));
        outer.add(inner, BorderLayout.CENTER);
        add(outer);

        setMinimumSize(new Dimension(600, 560));
        setSize(1000, 640);
        setLocationRelativeTo(null);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) user.requestFocusInWindow();
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    private void submit() {
        char[] typed = pass.getPassword();
        boolean empty = user.getText().trim().isEmpty() || typed.length == 0;
        boolean ok = user.getText().trim().equals(USERNAME) && Arrays.equals(typed, PASSWORD.toCharArray());
        Arrays.fill(typed, '\0');
        if (empty) {
            error.setText("Enter your username and password.");
            return;
        }
        if (!ok) {
            error.setText("Wrong username or password.");
            pass.setText("");
            pass.requestFocusInWindow();
            return;
        }
        dispose();
        onLogin.run();
    }
}
