package ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import model.SportMode;
import model.Tournament;

/**
 * First screen: the amber brand panel on the left (with the sport choice), the sign-in form on the right.
 * One fixed organizer account; the main window opens only after it matches.
 */
public class LoginFrame extends JFrame {
    // shortcut: one fixed login kept in code, fine for coursework, swap for stored accounts if real users are needed
    static final String USERNAME = "organizer";
    static final String PASSWORD = "1234";
    private static final int FORM_W = 420;
    private static final int PANEL_RADIUS = 32;
    private static final int PANEL_PAD = 40;

    private final JTextField user = new JTextField();
    private final JPasswordField pass = new JPasswordField();
    private final JLabel error = new JLabel(" ");
    private final Tournament tournament;
    private final Runnable onLogin;

    public LoginFrame(Tournament tournament, Runnable onLogin) {
        super("Tournament Bracket Manager - Sign in");
        this.tournament = tournament;
        this.onLogin = onLogin;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel split = new JPanel(new GridLayout(1, 2, Theme.SPACE_XL, 0));
        split.setBackground(Theme.BG);
        split.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL));
        split.add(buildBrandPanel());
        split.add(buildFormPanel());
        setContentPane(split);

        setMinimumSize(new Dimension(1000, 640));
        setSize(1440, 900);
        setLocationRelativeTo(null);
    }

    private JPanel buildBrandPanel() {
        JPanel panel = new RoundPanel(Theme.PRIMARY, null);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(PANEL_PAD, PANEL_PAD, PANEL_PAD, PANEL_PAD));

        JLabel name = new JLabel("Bracket Manager");
        name.setFont(Theme.display(Theme.SIZE_L));
        name.setForeground(Theme.ON_PRIMARY);
        name.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, Theme.SPACE_XS)); // antialiased bold text overruns its width
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_M, 0));
        brand.setOpaque(false);
        brand.add(new DarkLogo(Theme.CONTROL_H));
        brand.add(name);

        JLabel line = new JLabel("Single-elimination brackets for 4, 8 or 16 players or teams.");
        line.setFont(Theme.body(Theme.SIZE_M));
        line.setForeground(Theme.ON_PRIMARY);
        JLabel choose = new JLabel("Sport mode");
        choose.setFont(Theme.body(Theme.SIZE_S).deriveFont(Font.BOLD));
        choose.setForeground(Theme.ON_PRIMARY);

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_S, 0));
        pills.setOpaque(false);
        pills.getAccessibleContext().setAccessibleName("Sport mode");
        ButtonGroup group = new ButtonGroup();
        for (SportMode mode : SportMode.values()) {
            AmberPill pill = new AmberPill(mode.label);
            pill.setSelected(tournament.sport == mode);
            pill.addActionListener(e -> tournament.sport = mode);
            group.add(pill);
            pills.add(pill);
        }

        JPanel foot = new JPanel();
        foot.setOpaque(false);
        foot.setLayout(new BoxLayout(foot, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[] {line, (JComponent) Box.createVerticalStrut(Theme.SPACE_XL), choose,
                (JComponent) Box.createVerticalStrut(Theme.SPACE_S), pills}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            foot.add(c);
        }
        panel.add(brand, BorderLayout.NORTH);
        panel.add(foot, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new RoundPanel(Theme.PANEL, Theme.LINE);
        panel.setLayout(new GridBagLayout()); // centres the form column

        FieldStyle.apply(user);
        FieldStyle.apply(pass);
        user.getAccessibleContext().setAccessibleName("Username");
        pass.getAccessibleContext().setAccessibleName("Password");

        JLabel title = new JLabel("Sign in");
        title.setFont(Theme.display(Theme.SIZE_XXL));
        title.setForeground(Theme.INK);
        JLabel helper = new JLabel("Use the organizer account to open the tournament.");
        helper.setFont(Theme.body(Theme.SIZE_M));
        helper.setForeground(Theme.INK_MUTED);
        error.setFont(Theme.body(Theme.SIZE_S));
        error.setForeground(Theme.DANGER_TEXT);

        StyledButton login = new StyledButton("Sign in", StyledButton.Kind.PRIMARY);
        StyledButton exit = new StyledButton("Exit", StyledButton.Kind.DANGER);
        login.addActionListener(e -> submit());
        exit.addActionListener(e -> {
            if (Confirm.ask(this, "Exit", "Exit the application?", "Exit")) System.exit(0);
        });
        getRootPane().setDefaultButton(login);
        JPanel buttons = new JPanel(new GridBagLayout());
        buttons.setOpaque(false);
        GridBagConstraints b = new GridBagConstraints();
        b.fill = GridBagConstraints.HORIZONTAL;
        b.weightx = 2;
        buttons.add(login, b);
        b.weightx = 1;
        b.insets = new Insets(0, Theme.SPACE_S, 0, 0);
        buttons.add(exit, b);

        JLabel note = new JLabel("<html>Records stay in memory while the app is open. Exit asks before closing.</html>");
        note.setFont(Theme.body(Theme.SIZE_S));
        note.setForeground(Theme.INK_MUTED);
        note.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(Theme.BORDER, 0, 0, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(Theme.SPACE_L, 0, 0, 0)));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        Box.Filler width = new Box.Filler(new Dimension(FORM_W, 0), new Dimension(FORM_W, 0), new Dimension(FORM_W, 0));
        JComponent[] rows = {width, title, helper, (JComponent) Box.createVerticalStrut(Theme.SPACE_XL),
            FieldStyle.labeled("Username", user), (JComponent) Box.createVerticalStrut(Theme.SPACE_L),
            FieldStyle.labeled("Password", pass), (JComponent) Box.createVerticalStrut(Theme.SPACE_S), error,
            (JComponent) Box.createVerticalStrut(Theme.SPACE_M), buttons,
            (JComponent) Box.createVerticalStrut(Theme.SPACE_XL), note};
        for (JComponent c : rows) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            form.add(c);
        }
        panel.add(form);
        return panel;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) user.requestFocusInWindow();
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

    /** A large rounded panel: fill colour plus an optional 1px outline. */
    private static final class RoundPanel extends JPanel {
        private final java.awt.Color fill, edge;

        RoundPanel(java.awt.Color fill, java.awt.Color edge) {
            this.fill = fill;
            this.edge = edge;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 2 * PANEL_RADIUS, 2 * PANEL_RADIUS);
            if (edge != null) {
                g2.setColor(edge);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 2 * PANEL_RADIUS, 2 * PANEL_RADIUS);
            }
            g2.dispose();
        }
    }

    /** On the amber panel the logo inverts: a dark square with an amber trophy. */
    private static final class DarkLogo extends JComponent {
        private final int size;

        DarkLogo(int size) {
            this.size = size;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(size, size);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.BG);
            g2.fillRoundRect(0, 0, size, size, size / 2, size / 2);
            int glyph = size * 6 / 10;
            Glyphs.paint(g2, Glyphs.Kind.TROPHY, (size - glyph) / 2, (size - glyph) / 2, glyph, Theme.PRIMARY);
            g2.dispose();
        }
    }

    /** Sport choice on the amber panel: selected is a dark pill with amber text, the other an outline. */
    private static final class AmberPill extends JToggleButton {
        AmberPill(String text) {
            super(text);
            setFont(Theme.body(Theme.SIZE_M).deriveFont(Font.BOLD));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addItemListener(e -> getAccessibleContext().setAccessibleName(getText() + (isSelected() ? " (selected)" : "")));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 2 * Theme.SPACE_XL, Theme.CONTROL_H);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (isSelected()) {
                g2.setColor(Theme.BG);
                g2.fillRoundRect(0, 0, w, h, h, h);
            } else {
                g2.setColor(Theme.ON_PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, h - 2, h - 2);
            }
            g2.setFont(getFont());
            g2.setColor(isSelected() ? Theme.PRIMARY : Theme.ON_PRIMARY);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            if (isFocusOwner()) {
                // On the amber panel an amber ring would vanish, so the ring is dark and sits just inside the pill.
                g2.setColor(isSelected() ? Theme.PRIMARY : Theme.ON_PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(3, 3, w - 7, h - 7, h - 6, h - 6);
            }
            g2.dispose();
        }
    }
}
