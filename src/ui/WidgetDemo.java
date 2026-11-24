package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import model.Match;

/** One of each shared widget, for looking at. Run: java -cp out ui.WidgetDemo */
public class WidgetDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> build().setVisible(true));
    }

    static JFrame build() {
        JFrame frame = new JFrame("Widget demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel page = new JPanel(new BorderLayout(0, Theme.SPACE_L));
        page.setBackground(Theme.BG);
        page.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL));

        JLabel title = new JLabel("WIDGETS");
        title.setFont(Theme.display(48));
        title.setForeground(Theme.INK);
        page.add(title, BorderLayout.NORTH);

        JPanel stack = new JPanel(new BorderLayout(0, Theme.SPACE_L));
        stack.setOpaque(false);

        Card buttons = new Card("Buttons and toasts");
        buttons.body().setLayout(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_S, 0));
        StyledButton add = new StyledButton("Add", StyledButton.Kind.PRIMARY);
        StyledButton update = new StyledButton("Update");
        StyledButton delete = new StyledButton("Delete", StyledButton.Kind.DANGER);
        add.addActionListener(e -> Toast.success(frame.getRootPane(), "Player added."));
        delete.addActionListener(e -> Toast.error(frame.getRootPane(), "Pick a row to delete first."));
        buttons.body().add(add);
        buttons.body().add(update);
        buttons.body().add(delete);
        stack.add(buttons, BorderLayout.NORTH);

        Card matches = new Card("Matches");
        DefaultTableModel model = new DefaultTableModel(
                new Object[][] {
                    {"M001", 1, "Nicole", "Skirk", Match.Status.PLAYED},
                    {"M002", 1, "Flins", "Nahida", Match.Status.IN_PROGRESS},
                    {"M003", 2, "Skirk", "Nahida", Match.Status.READY},
                    {"M004", 3, "TBD", "TBD", Match.Status.WAITING},
                },
                new Object[] {"ID", "Round", "Player A", "Player B", "Status"}) {
            @Override
            public Class<?> getColumnClass(int c) {
                return c == 4 ? Match.Status.class : Object.class;
            }
        };
        JTable table = new JTable(model);
        TableStyle.apply(table);
        TableStyle.monoColumn(table, 0);
        table.setDefaultRenderer(Match.Status.class, new StatusPill());
        table.setRowSelectionInterval(1, 1);
        matches.body().add(TableStyle.scroll(table), BorderLayout.CENTER);
        stack.add(matches, BorderLayout.CENTER);

        page.add(stack, BorderLayout.CENTER);
        frame.setContentPane(page);
        frame.setSize(900, 620);
        frame.setLocationRelativeTo(null);
        return frame;
    }
}
