package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Match;
import model.Player;
import model.Tournament;

/** Matches: enter winners, add extra matches, delete unplayed extra matches, look up by ID, sort. */
public class MatchesPanel extends Page {
    private static final String CHOOSE = "Select";
    private static final String SORT_ROUND = "Round";
    private static final String SORT_ID = "ID";
    private static final int ROUND_WIDTH = 120;
    private static final int SORT_WIDTH = 200;
    private static final int COL_A = 2;
    private static final int COL_B = 3;
    private static final int COL_STATUS = 5;

    private final PageHeader header;
    private final Card formCard = new Card("Match details");
    private final Card listCard = new Card("");
    private final JPanel fields = new JPanel(new GridBagLayout());
    private JPanel buttons;
    private boolean narrow;
    private final JTextField roundField = new JTextField("1");
    private final JLabel labelA = new JLabel();
    private final JLabel labelB = new JLabel();
    private final JComboBox<Object> boxA = new JComboBox<>();
    private final JComboBox<Object> boxB = new JComboBox<>();
    private final JComboBox<Object> boxWinner = new JComboBox<>();
    private final JLabel formError = new JLabel(" ");
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> sortBox = new JComboBox<>(new String[] {SORT_ROUND, SORT_ID});
    private final JLabel note = new JLabel();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"ID", "Round", "A", "B", "Winner", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == COL_STATUS) return Match.Status.class;
            return String.class;
        }
    };
    private final JTable table = new JTable(model);

    private List<Match> shown = new ArrayList<>();
    private Match selected;
    private String query = "";
    private Match filled;    // the match the form was last filled from
    private boolean syncing;

    public MatchesPanel(Tournament tournament, Host host) {
        super(tournament, host);
        setLayout(new BorderLayout());
        header = new PageHeader();
        buildForm();
        buildList();

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        header.setAlignmentX(LEFT_ALIGNMENT);
        formCard.setAlignmentX(LEFT_ALIGNMENT);
        top.add(header);
        top.add(Box.createVerticalStrut(Theme.SPACE_XL));
        top.add(formCard);
        top.add(Box.createVerticalStrut(Theme.SPACE_L));

        ScrollContent content = new ScrollContent();
        content.add(top, BorderLayout.NORTH);
        content.add(listCard, BorderLayout.CENTER);
        add(ScrollContent.pane(content), BorderLayout.CENTER);
        refresh();
    }

    // ----- construction -----

    private void buildForm() {
        FieldStyle.apply(roundField);
        roundField.getAccessibleContext().setAccessibleName("Round");
        for (JComboBox<Object> box : List.of(boxA, boxB, boxWinner)) ComboStyle.apply(box);
        boxWinner.getAccessibleContext().setAccessibleName("Winner");
        formCard.setTitle("");

        formError.setFont(Theme.body(Theme.SIZE_S));
        formError.setForeground(Theme.DANGER_TEXT);

        StyledButton enter = new StyledButton("Enter winner", StyledButton.Kind.PRIMARY);
        StyledButton addBtn = new StyledButton("Add");
        StyledButton deleteBtn = new StyledButton("Delete", StyledButton.Kind.DANGER);
        StyledButton clearBtn = new StyledButton("Clear");
        enter.addActionListener(e -> update());
        addBtn.addActionListener(e -> add());
        deleteBtn.addActionListener(e -> delete());
        clearBtn.addActionListener(e -> clearFields());
        buttons = StyledButton.row(enter, addBtn, deleteBtn, clearBtn);

        fields.setOpaque(false);
        layoutFields();

        JPanel form = new JPanel(new BorderLayout(0, Theme.SPACE_XS));
        form.setOpaque(false);
        form.add(fields, BorderLayout.CENTER);
        form.add(formError, BorderLayout.SOUTH);
        formCard.body().add(form, BorderLayout.CENTER);
    }

    /** Round, both sides and the winner on one line, the actions at the end (or on a line of their own when narrow). */
    private void layoutFields() {
        fields.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.SOUTHWEST;
        JPanel roundPanel = FieldStyle.labeled("Round", roundField);
        roundPanel.setPreferredSize(new Dimension(ROUND_WIDTH, roundPanel.getPreferredSize().height));
        c.gridx = 0;
        fields.add(roundPanel, c);
        JComponent[] panels = {FieldStyle.labeled(labelA, boxA), FieldStyle.labeled(labelB, boxB),
            FieldStyle.labeled("Winner", boxWinner)};
        for (JComponent p : panels) {
            c.gridx++;
            c.weightx = 1;
            c.insets = new Insets(0, Theme.SPACE_M, 0, 0);
            fields.add(p, c);
        }
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        if (narrow) {
            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = 4;
            c.insets = new Insets(Theme.SPACE_L, 0, 0, 0);
        } else {
            c.gridx++;
            c.insets = new Insets(0, Theme.SPACE_L, 0, 0);
        }
        fields.add(buttons, c);
        fields.revalidate();
        fields.repaint();
    }

    @Override
    public void resized(int frameWidth) {
        boolean one = frameWidth < 1280;   // the action buttons need about 430 px beside four fields
        if (one == narrow) return;
        narrow = one;
        layoutFields();
    }

    private void buildList() {
        FieldStyle.apply(searchField);
        searchField.getAccessibleContext().setAccessibleName("Search by match ID");
        searchField.addActionListener(e -> search()); // Enter searches
        StyledButton searchBtn = new StyledButton("Search");
        searchBtn.addActionListener(e -> search());
        ComboStyle.apply(sortBox);
        sortBox.getAccessibleContext().setAccessibleName("Sort by");
        sortBox.addActionListener(e -> reload(selected));

        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        toolbar.add(FieldStyle.labeled("Search by match ID (for example M013)", searchField), c);
        searchField.setToolTipText("For example M013");
        c.gridx = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTH;
        c.insets = new Insets(0, Theme.SPACE_S, 0, 0);
        toolbar.add(searchBtn, c);
        c.gridx = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, Theme.SPACE_L, 0, 0);
        JPanel sortPanel = FieldStyle.labeled("Sort by", sortBox);
        sortPanel.setPreferredSize(new Dimension(SORT_WIDTH, sortPanel.getPreferredSize().height));
        toolbar.add(sortPanel, c);

        TableStyle.apply(table);
        table.setDefaultRenderer(Match.Status.class, new StatusPill());
        TableStyle.idColumn(table, 0);
        TableStyle.column(table, 4, Theme.body(Theme.SIZE_M).deriveFont(java.awt.Font.BOLD),
                v -> "-".equals(v) ? Theme.INK_MUTED : Theme.PRIMARY);   // the winner, in amber
        int[] widths = {80, 110, 170, 170, 170, 130};
        for (int col = 0; col < widths.length; col++) table.getColumnModel().getColumn(col).setPreferredWidth(widths[col]);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getAccessibleContext().setAccessibleName("Matches table");
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || syncing) return;
            int row = table.getSelectedRow();
            selected = row >= 0 && row < shown.size() ? shown.get(row) : null;
            if (selected != null) fillForm(selected);
            filled = selected;
            clearError();
        });
        JPanel tableScroll = TableStyle.box(TableStyle.scroll(table));
        note.setFont(Theme.body(Theme.SIZE_S));
        note.setForeground(Theme.INK_MUTED);
        note.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_S, 0, 0, 0));

        JPanel body = new JPanel(new BorderLayout(0, Theme.SPACE_L));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);
        body.add(tableScroll, BorderLayout.CENTER);
        body.add(note, BorderLayout.SOUTH);
        listCard.body().add(body, BorderLayout.CENTER);
    }

    // ----- actions -----

    /** Adds an extra match. It counts for points but does not feed the bracket. */
    private void add() {
        try {
            int round = parseRound();
            Player a = chosen(boxA), b = chosen(boxB), winner = chosen(boxWinner);
            if (a == null || b == null) throw new IllegalArgumentException("Pick both sides");
            if (winner != null && winner != a && winner != b) {
                throw new IllegalArgumentException("The winner must be " + a.name + " or " + b.name);
            }
            Match m = tournament.addExtraMatch(round, a, b);
            String id = Tournament.matchId(m);
            if (winner != null) {
                tournament.setWinner(m, winner);
                succeed("Added " + id + ": " + winner.name + " beat " + (winner == a ? b : a).name + ".");
            } else {
                succeed("Added " + id + ": " + a.name + " vs " + b.name + ".");
            }
            reload(m);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            fail(ex.getMessage());
        }
    }

    /** Update enters the winner of the selected match. */
    private void update() {
        if (selected == null) {
            fail("Select a match to update");
            return;
        }
        Player winner = chosen(boxWinner);
        if (winner == null) {
            fail("Pick the winner");
            return;
        }
        try {
            tournament.setWinner(selected, winner);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            fail(ex.getMessage());
            return;
        }
        String message = Tournament.matchId(selected) + ": " + winner.name + " wins";
        if (selected.next != null) message += " and moves on to " + Tournament.matchId(selected.next);
        else if (tournament.champion() == winner) message += ". Champion";
        succeed(message + ".");
        reload(selected);
    }

    private void delete() {
        if (selected == null) {
            fail("Select a match to delete");
            return;
        }
        if (selected.bracket) {
            fail("Bracket matches cannot be deleted");
            return;
        }
        if (selected.status == Match.Status.PLAYED || selected.status == Match.Status.IN_PROGRESS) {
            fail("Only unplayed matches can be deleted");
            return;
        }
        String id = Tournament.matchId(selected);
        if (!Confirm.ask(this, "Delete match", "Delete " + id + "? The match is removed and this cannot be undone.", "Delete")) {
            return;
        }
        try {
            tournament.removeMatch(selected);
        } catch (IllegalStateException ex) {
            fail(ex.getMessage());
            return;
        }
        succeed("Deleted " + id + ".");
        clearForm();
        reload(null);
    }

    private void clearFields() {
        filled = null;
        clearForm();
        searchField.setText("");
        query = "";
        table.clearSelection();
        selected = null;
        reload(null);
    }

    /** Looks the typed ID up with binary search (Tournament.findMatch). An empty box shows all matches. */
    private void search() {
        query = searchField.getText().trim();
        Match found = query.isEmpty() ? null : tournament.findMatch(query);
        if (!query.isEmpty() && found == null) {
            host.notify("No match with ID " + query + ".", true);
        } else if (found != null) {
            host.notify("Found " + Tournament.matchId(found) + ".", false);
        }
        reload(found);
    }

    private void succeed(String message) {
        clearError();
        host.notify(message, false);
    }

    private void fail(String message) {
        String text = message.endsWith(".") ? message : message + ".";
        formError.setText(text);
        host.notify(text, true);
    }

    private void clearError() {
        formError.setText(" ");
    }

    private int parseRound() {
        try {
            int r = Integer.parseInt(roundField.getText().trim());
            if (r >= 1) return r;
        } catch (NumberFormatException ignored) {
            // falls through to the message below
        }
        throw new IllegalArgumentException("Round must be a whole number of 1 or more");
    }

    private static Player chosen(JComboBox<Object> box) {
        return box.getSelectedItem() instanceof Player p ? p : null;
    }

    private void clearForm() {
        roundField.setText("1");
        boxA.setSelectedIndex(0);
        boxB.setSelectedIndex(0);
        boxWinner.setSelectedIndex(0);
        clearError();
    }

    private void fillForm(Match m) {
        roundField.setText(String.valueOf(m.round));
        select(boxA, m.a);
        select(boxB, m.b);
        select(boxWinner, m.winner);
    }

    private static void select(JComboBox<Object> box, Player p) {
        box.setSelectedItem(p == null ? CHOOSE : p);
    }

    // ----- drawing the data -----

    @Override
    public String title() {
        return "Matches";
    }

    @Override
    public void refresh() {
        String entity = tournament.sport.entity;
        labelA.setText(entity + " A");
        labelB.setText(entity + " B");
        boxA.getAccessibleContext().setAccessibleName(entity + " A"); // same words as the visible labels
        boxB.getAccessibleContext().setAccessibleName(entity + " B");
        table.getColumnModel().getColumn(COL_A).setHeaderValue(entity + " A");
        table.getColumnModel().getColumn(COL_B).setHeaderValue(entity + " B");
        table.getTableHeader().repaint();
        fillChoices(boxA);
        fillChoices(boxB);
        fillChoices(boxWinner);
        if (selected != null && !tournament.matches().list().contains(selected)) selected = null;
        reload(selected);
    }

    /** Reloads one drop-down with "Select" plus every player, keeping the current pick. */
    private void fillChoices(JComboBox<Object> box) {
        Object keep = box.getSelectedItem();
        box.removeAllItems();
        box.addItem(CHOOSE);
        for (Player p : tournament.players().list()) box.addItem(p);
        box.setSelectedItem(keep instanceof Player p && tournament.players().list().contains(p) ? p : CHOOSE);
    }

    /** The rows to show: one match when a search is active, else all, sorted. */
    private List<Match> visible() {
        if (!query.isEmpty()) {
            Match hit = tournament.findMatch(query);
            return hit == null ? new ArrayList<>() : new ArrayList<>(List.of(hit));
        }
        if (SORT_ROUND.equals(sortBox.getSelectedItem())) return tournament.matchesByRound(); // merge sort
        return new ArrayList<>(tournament.matches().list()); // list order is already by ID
    }

    private void reload(Match select) {
        syncing = true;
        shown = visible();
        model.setRowCount(0);
        int selectRow = -1;
        for (int i = 0; i < shown.size(); i++) {
            Match m = shown.get(i);
            model.addRow(new Object[] {Tournament.matchId(m), tournament.roundName(m, false), name(m.a), name(m.b),
                m.winner == null ? "-" : m.winner.name, m.status});
            if (m == select) selectRow = i;
        }
        if (selectRow >= 0) table.setRowSelectionInterval(selectRow, selectRow);
        else table.clearSelection();
        selected = selectRow >= 0 ? select : null;
        if (selected != null && selected != filled) {   // keep unsaved form edits on sort, search and refresh
            fillForm(selected);
            filled = selected;
        }
        syncing = false;

        int count = tournament.matches().size();
        header.set(title(), count + (count == 1 ? " match" : " matches")
                + (selected == null ? "" : " · editing " + Tournament.matchId(selected)));
        if (shown.isEmpty()) {
            note.setText(query.isEmpty() ? "No matches yet. Generate the bracket or add an extra match."
                    : "No match with ID " + query + ".");
        } else {
            note.setText("Enter winner records the result. Add creates an extra match; only unplayed extra matches can be deleted.");
        }
    }

    private static String name(Player p) {
        return p == null ? "TBD" : p.name;
    }
}
