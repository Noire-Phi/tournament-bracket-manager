package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import model.Match;

/**
 * Component sheet: every token and shared widget on one screen, so a change to Theme can be checked at a glance.
 * Run: java -cp out ui.WidgetDemo
 */
public class WidgetDemo {
    public static void main(String[] args) {
        Main.styleDialogs();
        SwingUtilities.invokeLater(() -> build().setVisible(true));
    }

    static JFrame build() {
        JFrame frame = new JFrame("Components");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel page = new JPanel(new BorderLayout(0, Theme.SPACE_XL));
        page.setBackground(Theme.BG);
        page.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL));
        PageHeader header = new PageHeader();
        header.set("Components", "Every colour, size and control comes from Theme");
        page.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, Theme.SPACE_L, Theme.SPACE_L));
        grid.setOpaque(false);
        grid.add(colours());
        grid.add(type());
        grid.add(buttons());
        grid.add(fields(frame));
        grid.add(pills());
        grid.add(feedback(frame));
        ScrollContent content = new ScrollContent(); // at 1000 x 640 the six tiles scroll instead of clipping
        content.add(grid, BorderLayout.CENTER);
        page.add(ScrollContent.pane(content), BorderLayout.CENTER);

        frame.setContentPane(page);
        frame.setSize(1440, 900);
        frame.setLocationRelativeTo(null);
        return frame;
    }

    private static Card colours() {
        Card card = new Card("Colour");
        JPanel list = new JPanel(new GridLayout(0, 2, Theme.SPACE_M, Theme.SPACE_S));
        list.setOpaque(false);
        Object[][] swatches = {
            {"Background", Theme.BG}, {"Tile", Theme.PANEL}, {"Inner", Theme.PANEL_DEEP}, {"Line", Theme.LINE},
            {"Ink", Theme.INK}, {"Muted", Theme.INK_MUTED}, {"Amber", Theme.PRIMARY}, {"Live / danger", Theme.LIVE},
        };
        for (Object[] s : swatches) list.add(swatch((String) s[0], (Color) s[1]));
        card.body().add(list, BorderLayout.NORTH);
        return card;
    }

    private static JComponent swatch(String name, Color colour) {
        JLabel label = new JLabel(name);
        label.setFont(Theme.body(Theme.SIZE_S));
        label.setForeground(Theme.INK);
        label.setIconTextGap(Theme.SPACE_S);
        label.setIcon(new javax.swing.Icon() {
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colour);
                g2.fillRoundRect(x, y, 28, 28, 10, 10);
                g2.setColor(Theme.LINE);
                g2.drawRoundRect(x, y, 27, 27, 10, 10);
                g2.dispose();
            }

            public int getIconWidth() {
                return 28;
            }

            public int getIconHeight() {
                return 28;
            }
        });
        return label;
    }

    private static Card type() {
        Card card = new Card("Type");
        JPanel list = column();
        list.add(text("Bracket", Theme.display(Theme.SIZE_XXL), Theme.INK));
        list.add(text("Semifinals · 32", Theme.display(Theme.SIZE_XL), Theme.INK));
        list.add(text("Tile title · 20", Theme.display(Theme.SIZE_L), Theme.INK));
        list.add(text("Body text in DM Sans · 16", Theme.body(Theme.SIZE_M), Theme.INK));
        list.add(text("Caption and IDs · M013 · 14", Theme.body(Theme.SIZE_S), Theme.INK_MUTED));
        card.body().add(list, BorderLayout.NORTH);
        return card;
    }

    private static Card buttons() {
        Card card = new Card("Buttons");
        StyledButton disabled = new StyledButton("Disabled");
        disabled.setEnabled(false);
        JPanel list = column();
        list.add(StyledButton.row(new StyledButton("Primary", StyledButton.Kind.PRIMARY), new StyledButton("Secondary")));
        list.add(StyledButton.row(new StyledButton("Danger", StyledButton.Kind.DANGER),
                new StyledButton("Ghost", StyledButton.Kind.GHOST)));
        list.add(StyledButton.row(disabled));
        list.add(text("Tab to see the amber focus ring.", Theme.body(Theme.SIZE_S), Theme.INK_MUTED));
        card.body().add(list, BorderLayout.NORTH);
        return card;
    }

    private static Card fields(JFrame frame) {
        Card card = new Card("Fields");
        JTextField normal = new JTextField("Nicole");
        JTextField focused = new JTextField("Furina");
        JTextField error = new JTextField("");
        FieldStyle.apply(normal);
        FieldStyle.apply(focused);
        FieldStyle.apply(error);
        FieldStyle.setError(error, true);
        JPanel list = column();
        list.add(FieldStyle.labeled("Default", normal));
        list.add(FieldStyle.labeled("Focused", focused));
        list.add(FieldStyle.labeled("Error", error));
        list.add(text("Type a name to add.", Theme.body(Theme.SIZE_S), Theme.DANGER_TEXT));
        card.body().add(list, BorderLayout.NORTH);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                focused.requestFocusInWindow();
            }
        });
        return card;
    }

    private static Card pills() {
        Card card = new Card("Status");
        DefaultTableModel model = new DefaultTableModel(
                new Object[][] {
                    {"M001", Match.Status.PLAYED},
                    {"M013", Match.Status.IN_PROGRESS},
                    {"M014", Match.Status.READY},
                    {"M015", Match.Status.WAITING},
                },
                new Object[] {"ID", "Status"}) {
            @Override
            public Class<?> getColumnClass(int c) {
                return c == 1 ? Match.Status.class : Object.class;
            }
        };
        JTable table = new JTable(model);
        TableStyle.apply(table);
        TableStyle.idColumn(table, 0);
        table.setDefaultRenderer(Match.Status.class, new StatusPill());
        card.body().add(TableStyle.box(TableStyle.scroll(table)), BorderLayout.CENTER);
        return card;
    }

    private static Card feedback(JFrame frame) {
        Card card = new Card("Toasts and confirm");
        StyledButton ok = new StyledButton("Show success");
        StyledButton bad = new StyledButton("Show error");
        StyledButton ask = new StyledButton("Delete confirm", StyledButton.Kind.DANGER);
        StyledButton safe = new StyledButton("Safe confirm");
        StyledButton pick = new StyledButton("Picker");
        ok.addActionListener(e -> Toast.success(frame.getRootPane(), "Player added."));
        bad.addActionListener(e -> Toast.error(frame.getRootPane(), "Pick a row to delete first."));
        ask.addActionListener(e -> Confirm.ask(frame, "Delete player", "Delete Nicole? This cannot be undone.", "Delete"));
        safe.addActionListener(e -> Confirm.ask(frame, "Generate bracket",
                "Generate the bracket for 16 players? The list is locked afterwards.", "Generate", false));
        pick.addActionListener(e -> Confirm.choose(frame, "Enter result", "Who won M013?", "Nicole", "Furina"));
        JPanel list = column();
        list.add(StyledButton.row(ok, bad));
        list.add(StyledButton.row(ask, safe, pick));
        list.add(text("Toasts fade after a few seconds.", Theme.body(Theme.SIZE_S),
                Theme.INK_MUTED));
        card.body().add(list, BorderLayout.NORTH);
        return card;
    }

    private static JPanel column() {
        JPanel p = new JPanel() {
            @Override
            protected void addImpl(java.awt.Component comp, Object constraints, int index) {
                ((JComponent) comp).setAlignmentX(0f); // BoxLayout lines up mixed rows only when all share one alignment
                super.addImpl(comp, constraints, index);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static JComponent text(String s, java.awt.Font font, Color colour) {
        JLabel label = new JLabel(s);
        label.setFont(font);
        label.setForeground(colour);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SPACE_S, 0));
        label.setAlignmentX(0f);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
        return label;
    }
}
