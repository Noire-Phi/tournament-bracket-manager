package ui;

import algo.Searches;
import algo.Sorts;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.Player;
import model.Tournament;

/**
 * Players (Teams in physical-sports mode). Left: the details tile (ID, name, read-only stats, actions).
 * Right: the list tile with search and sort above the table. Under 1100 px wide the two tiles stack.
 */
public class PlayersPanel extends Page {
    private static final int FORM_W = 400;
    private static final int SORT_W = 220;
    private static final int ONE_COLUMN_BELOW = 1100;
    private static final String SORT_ID = "ID";
    private static final String SORT_POINTS = "Points (ties by name)";
    private static final String STILL_IN = "Still in";
    private static final String OUT = "Out";

    private final PageHeader header = new PageHeader();
    private final Card formCard = new Card("Details");
    private final Card listCard = new Card("");
    private final JPanel columns = new JPanel(new GridBagLayout());
    private final JLabel nameLabel = new JLabel();
    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JLabel fieldError = new JLabel(" ");
    private final JLabel lockNote = new JLabel();
    private final Stat points = new Stat("Points", true);
    private final Stat wins = new Stat("Wins", false);
    private final Stat losses = new Stat("Losses", false);
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> sortBox = new JComboBox<>(new String[] {SORT_ID, SORT_POINTS});
    private final JLabel note = new JLabel();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"ID", "Name", "Bracket", "Pts", "W", "L"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column >= 3 ? Integer.class : String.class;
        }
    };
    private final JTable table = new JTable(model);

    private List<Player> shown = new ArrayList<>();
    private Player selected;
    private boolean syncing;
    private boolean narrow;

    public PlayersPanel(Tournament tournament, Host host) {
        super(tournament, host);
        setLayout(new BorderLayout());
        buildForm();
        buildList();
        columns.setOpaque(false);
        layoutColumns();

        ScrollContent content = new ScrollContent();
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SPACE_XL, 0));
        content.add(header, BorderLayout.NORTH);
        content.add(columns, BorderLayout.CENTER);
        add(ScrollContent.pane(content), BorderLayout.CENTER);
        refresh();
    }

    // ----- construction -----

    private void buildForm() {
        FieldStyle.apply(idField);
        idField.setEditable(false);
        idField.setFocusable(false);                // shown, not edited: IDs are assigned on Add
        idField.setForeground(Theme.INK_MUTED);
        idField.setBackground(Theme.BG);
        idField.getAccessibleContext().setAccessibleName("ID");
        FieldStyle.apply(nameField);
        nameField.addActionListener(e -> {
            if (selected == null) add();            // Enter adds a new name, or saves a rename
            else update();
        });

        fieldError.setFont(Theme.body(Theme.SIZE_S));
        fieldError.setForeground(Theme.DANGER_TEXT);
        lockNote.setFont(Theme.body(Theme.SIZE_S));
        lockNote.setForeground(Theme.INK_MUTED);

        JPanel stats = new JPanel(new GridLayout(1, 3, Theme.SPACE_S, 0));
        stats.setOpaque(false);
        stats.add(points);
        stats.add(wins);
        stats.add(losses);

        StyledButton update = new StyledButton("Update", StyledButton.Kind.PRIMARY);
        StyledButton addBtn = new StyledButton("Add");
        StyledButton delete = new StyledButton("Delete", StyledButton.Kind.DANGER);
        StyledButton clear = new StyledButton("Clear");
        update.addActionListener(e -> update());
        addBtn.addActionListener(e -> add());
        delete.addActionListener(e -> delete());
        clear.addActionListener(e -> clearFields());
        JPanel buttons = new JPanel(new GridLayout(2, 2, Theme.SPACE_S, Theme.SPACE_S));
        buttons.setOpaque(false);
        buttons.add(update);
        buttons.add(addBtn);
        buttons.add(delete);
        buttons.add(clear);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        JComponent[] rows = {FieldStyle.labeled("ID", idField), gap(Theme.SPACE_L),
            FieldStyle.labeled(nameLabel, nameField), gap(Theme.SPACE_XS), fieldError, gap(Theme.SPACE_S),
            stats, gap(Theme.SPACE_XL), buttons, gap(Theme.SPACE_XL), lockNote};
        for (JComponent c : rows) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            form.add(c);
        }
        formCard.body().add(form, BorderLayout.NORTH);
    }

    private static JComponent gap(int px) {
        return (JComponent) Box.createVerticalStrut(px);
    }

    private void buildList() {
        FieldStyle.apply(searchField);
        searchField.getAccessibleContext().setAccessibleName("Search by name");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reload(selected);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reload(selected);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reload(selected);
            }
        });
        ComboStyle.apply(sortBox);
        sortBox.getAccessibleContext().setAccessibleName("Sort by");
        sortBox.addActionListener(e -> reload(selected));

        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        toolbar.add(FieldStyle.labeled("Search by name", searchField), c);
        c.gridx = 1;
        c.weightx = 0;
        c.insets = new Insets(0, Theme.SPACE_L, 0, 0);
        JPanel sortPanel = FieldStyle.labeled("Sort by", sortBox);
        sortPanel.setPreferredSize(new Dimension(SORT_W, sortPanel.getPreferredSize().height));
        toolbar.add(sortPanel, c);

        TableStyle.apply(table);
        TableStyle.idColumn(table, 0);
        TableStyle.column(table, 1, Theme.body(Theme.SIZE_M).deriveFont(Font.BOLD), v -> Theme.INK);
        TableStyle.column(table, 2, Theme.body(Theme.SIZE_M).deriveFont(Font.BOLD),
                v -> STILL_IN.equals(v) ? Theme.PRIMARY : Theme.INK_MUTED);
        int[] widths = {80, 200, 110, 64, 64, 64};
        for (int col = 0; col < widths.length; col++) table.getColumnModel().getColumn(col).setPreferredWidth(widths[col]);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || syncing) return;
            int row = table.getSelectedRow();
            selected = row >= 0 && row < shown.size() ? shown.get(row) : null;
            fillForm();
            clearError();
        });
        JPanel tableBox = TableStyle.box(TableStyle.scroll(table));
        tableBox.setPreferredSize(new Dimension(0, Theme.CONTROL_H * 9));

        note.setFont(Theme.body(Theme.SIZE_S));
        note.setForeground(Theme.INK_MUTED);

        JPanel body = new JPanel(new BorderLayout(0, Theme.SPACE_L));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);
        body.add(tableBox, BorderLayout.CENTER);
        body.add(note, BorderLayout.SOUTH);
        listCard.body().add(body, BorderLayout.CENTER);
    }

    /** Two columns: a fixed-width form tile and a list tile that takes the rest. One column when narrow. */
    private void layoutColumns() {
        columns.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        if (narrow) {
            c.gridx = 0;
            c.weightx = 1;
            c.weighty = 0;
            c.insets = new Insets(0, 0, Theme.SPACE_L, 0);
            columns.add(formCard, c);
            c.gridy = 1;
            c.weighty = 1;
            c.insets = new Insets(0, 0, 0, 0);
            columns.add(listCard, c);
        } else {
            formCard.setPreferredSize(new Dimension(FORM_W, formCard.getPreferredSize().height));
            c.weightx = 0;
            c.insets = new Insets(0, 0, 0, Theme.SPACE_L);
            columns.add(formCard, c);
            c.weightx = 1;
            c.insets = new Insets(0, 0, 0, 0);
            columns.add(listCard, c);
        }
        if (narrow) formCard.setPreferredSize(null);
        columns.revalidate();
        columns.repaint();
    }

    @Override
    public void resized(int frameWidth) {
        boolean one = frameWidth < ONE_COLUMN_BELOW;
        if (one == narrow) return;
        narrow = one;
        layoutColumns();
    }

    // ----- actions -----

    private void add() {
        try {
            Player p = tournament.addPlayer(nameField.getText());
            succeed("Added " + p.name + " as " + Tournament.playerId(p) + ".");
            reload(p);
            fillForm();
            nameField.requestFocusInWindow();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            fail(ex.getMessage());
        }
    }

    private void update() {
        if (selected == null) {
            fail("Select a row to update");
            return;
        }
        try {
            tournament.renamePlayer(selected, nameField.getText());
            succeed("Renamed to " + selected.name + ".");
            reload(selected);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            fail(ex.getMessage());
        }
    }

    private void delete() {
        if (selected == null) {
            fail("Select a row to delete");
            return;
        }
        if (tournament.isGenerated()) {
            fail(title() + " are locked once the bracket exists");
            return;
        }
        String entity = tournament.sport.entity;
        String message = "Delete " + selected.name + " (" + Tournament.playerId(selected) + ")? "
                + "The " + entity.toLowerCase() + " is removed from the list and this cannot be undone.";
        if (!Confirm.ask(this, "Delete " + entity.toLowerCase(), message, "Delete")) return;
        String name = selected.name;
        try {
            tournament.removePlayer(selected);
        } catch (IllegalStateException ex) {
            fail(ex.getMessage());
            return;
        }
        succeed("Deleted " + name + ".");
        reload(null);
        fillForm();
    }

    private void clearFields() {
        searchField.setText("");
        table.clearSelection();
        selected = null;
        fillForm();
        clearError();
        nameField.requestFocusInWindow();
    }

    private void succeed(String message) {
        clearError();
        host.notify(message, false);
    }

    private void fail(String message) {
        String text = message.endsWith(".") ? message : message + ".";
        fieldError.setText(text);
        FieldStyle.setError(nameField, true);
        host.notify(text, true);
    }

    private void clearError() {
        fieldError.setText(" ");
        FieldStyle.setError(nameField, false);
    }

    /** The form shows the selected row, or an empty "new" state. */
    private void fillForm() {
        if (selected == null) {
            idField.setText("New");
            nameField.setText("");
            points.set("-");
            wins.set("-");
            losses.set("-");
        } else {
            idField.setText(Tournament.playerId(selected));
            nameField.setText(selected.name);
            points.set(String.valueOf(selected.points));
            wins.set(String.valueOf(selected.wins));
            losses.set(String.valueOf(selected.losses));
        }
    }

    // ----- drawing the data -----

    @Override
    public String title() {
        return tournament.sport.entity + "s";
    }

    @Override
    public void refresh() {
        String plural = title().toLowerCase();
        formCard.setTitle(tournament.sport.entity + " details");
        table.getAccessibleContext().setAccessibleName(title() + " table");
        nameLabel.setText(tournament.sport.nameLabel);
        nameField.getAccessibleContext().setAccessibleName(tournament.sport.nameLabel); // same words as the visible label
        lockNote.setText(tournament.isGenerated()
                ? "Locked while the bracket runs." : " ");
        if (selected != null && !tournament.players().list().contains(selected)) selected = null;
        reload(selected);
        fillForm();
    }

    /** The rows to show: sorted, then filtered by the search text (a linear scan). */
    private List<Player> visible() {
        List<Player> rows = new ArrayList<>(tournament.players().list());
        if (SORT_POINTS.equals(sortBox.getSelectedItem())) Sorts.byPoints(rows); // list order is already by ID
        return Searches.linearContains(rows, searchField.getText());
    }

    private void reload(Player select) {
        syncing = true;
        shown = visible();
        model.setRowCount(0);
        int selectRow = -1;
        for (int i = 0; i < shown.size(); i++) {
            Player p = shown.get(i);
            String bracket = !tournament.isGenerated() ? "-" : tournament.isOut(p) ? OUT : STILL_IN;
            model.addRow(new Object[] {Tournament.playerId(p), p.name, bracket, p.points, p.wins, p.losses});
            if (p == select) selectRow = i;
        }
        if (selectRow >= 0) table.setRowSelectionInterval(selectRow, selectRow);
        else table.clearSelection();
        selected = selectRow >= 0 ? select : null;
        syncing = false;

        String plural = title().toLowerCase();
        int count = tournament.players().size();
        header.set(title(), count + " " + (count == 1 ? tournament.sport.entity.toLowerCase() : plural)
                + " · select a row to edit it");
        String query = searchField.getText().trim();
        note.setText(shown.isEmpty()
                ? (query.isEmpty() ? "No " + plural + " yet. Type a name and press Add." : "No " + plural + " match \"" + query + "\".")
                : " ");
    }

    /** A read-only stat: big number over a small label, on the inner surface. */
    private static final class Stat extends JPanel {
        private static final int RADIUS = 16;
        private final JLabel value = new JLabel("-");
        private final String name;

        Stat(String name, boolean accent) {
            super(new BorderLayout(0, Theme.SPACE_XS));
            this.name = name;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_M, Theme.SPACE_S, Theme.SPACE_M, Theme.SPACE_S));
            value.setFont(Theme.display(Theme.SIZE_XL));
            value.setForeground(accent ? Theme.PRIMARY : Theme.INK);
            value.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel label = new JLabel(name);
            label.setFont(Theme.body(Theme.SIZE_S));
            label.setForeground(Theme.INK_MUTED);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            add(value, BorderLayout.CENTER);
            add(label, BorderLayout.SOUTH);
        }

        void set(String text) {
            value.setText(text);
            getAccessibleContext().setAccessibleName(name + " " + text);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PANEL_DEEP);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 2 * RADIUS, 2 * RADIUS);
            g2.dispose();
        }
    }
}
