package ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import ds.ResultStack;
import model.Match;
import model.Tournament;

/**
 * History: every result, newest first. The list is the Undo stack, so the top row is what Undo reverses.
 * The "Next undo" tile spells out what Undo will change before you press it.
 */
public class HistoryPanel extends Page {
    private static final int ONE_COLUMN_BELOW = 1100;
    private static final int ROW_RADIUS = 12;

    private final PageHeader header = new PageHeader();
    private final JPanel list = new JPanel();
    private final Card listCard = new Card("");
    private final Card undoCard = new Card("Next undo");
    private final JPanel columns = new JPanel(new GridBagLayout());
    private final JLabel undoMeta = new JLabel();
    private final JLabel undoWinner = new JLabel();
    private final JLabel undoRest = new JLabel();
    private final JPanel effects = new JPanel();
    private final StyledButton undoButton = new StyledButton("Undo", StyledButton.Kind.DANGER);
    private final JPanel undoBody = new JPanel(new BorderLayout(0, Theme.SPACE_XL));
    private final JLabel empty = new JLabel("Nothing to undo yet. Results appear here as they are entered.");
    private boolean narrow;

    public HistoryPanel(Tournament tournament, Host host) {
        super(tournament, host);
        setLayout(new BorderLayout());

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setFocusable(true);   // so the arrow keys can scroll a long history
        list.getAccessibleContext().setAccessibleName("Results, newest first");
        JScrollPane scroll = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        listCard.body().add(scroll, BorderLayout.CENTER);

        undoButton.addActionListener(e -> {
            if (UndoAction.run(this, tournament, host)) refresh();
        });
        buildUndoTile();
        columns.setOpaque(false);
        layoutColumns();

        header.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SPACE_XL, 0));
        add(header, BorderLayout.NORTH);
        ScrollContent content = new ScrollContent(); // stacked below 1100px, the page scrolls instead of clipping
        content.add(columns, BorderLayout.CENTER);
        add(ScrollContent.pane(content), BorderLayout.CENTER);
        refresh();
    }

    private void buildUndoTile() {
        undoMeta.setFont(Theme.body(Theme.SIZE_S));
        undoMeta.setForeground(Theme.INK_MUTED);
        undoWinner.setFont(Theme.display(Theme.SIZE_XL));
        undoWinner.setForeground(Theme.PRIMARY);
        undoRest.setFont(Theme.display(Theme.SIZE_XL));
        undoRest.setForeground(Theme.INK);
        JPanel headline = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headline.setOpaque(false);
        headline.add(undoWinner);
        headline.add(undoRest);
        JPanel result = new Inner();
        result.setLayout(new BorderLayout(0, Theme.SPACE_XS));
        result.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_L, Theme.SPACE_L, Theme.SPACE_L, Theme.SPACE_L));
        result.add(undoMeta, BorderLayout.NORTH);
        result.add(headline, BorderLayout.CENTER);

        effects.setOpaque(false);
        effects.setLayout(new BoxLayout(effects, BoxLayout.Y_AXIS));
        JPanel top = new JPanel(new BorderLayout(0, Theme.SPACE_L));
        top.setOpaque(false);
        top.add(result, BorderLayout.NORTH);
        top.add(effects, BorderLayout.CENTER);
        top.add(undoButton, BorderLayout.SOUTH);

        empty.setFont(Theme.body(Theme.SIZE_M));
        empty.setForeground(Theme.INK_MUTED);
        undoBody.setOpaque(false);
        undoBody.add(top, BorderLayout.NORTH);
        undoCard.body().add(undoBody, BorderLayout.CENTER);
    }

    private void layoutColumns() {
        columns.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        if (narrow) {
            c.gridx = 0;
            c.weightx = 1;
            c.insets = new Insets(0, 0, Theme.SPACE_L, 0);
            columns.add(undoCard, c);
            c.gridy = 1;
            c.insets = new Insets(0, 0, 0, 0);
            columns.add(listCard, c);
        } else {
            listCard.setPreferredSize(new Dimension(0, 0));
            undoCard.setPreferredSize(new Dimension(0, 0));
            c.weightx = 0.64;
            c.insets = new Insets(0, 0, 0, Theme.SPACE_L);
            columns.add(listCard, c);
            c.weightx = 0.36;
            c.insets = new Insets(0, 0, 0, 0);
            columns.add(undoCard, c);
        }
        if (narrow) {
            listCard.setPreferredSize(null);
            undoCard.setPreferredSize(null);
        }
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

    @Override
    public String title() {
        return "History";
    }

    @Override
    public void refresh() {
        List<ResultStack.Entry> entries = tournament.results().newestFirst();
        header.set(title(), entries.isEmpty() ? "No results yet"
                : entries.size() + (entries.size() == 1 ? " result" : " results")
                        + " · newest first · the top one is the next to undo");
        list.removeAll();
        for (int i = 0; i < entries.size(); i++) {
            ResultStack.Entry e = entries.get(i);
            Row row = new Row(entries.size() - i, e, tournament.roundName(e.match, false), i == 0);
            row.setAlignmentX(LEFT_ALIGNMENT);
            if (i > 0) list.add(Box.createVerticalStrut(Theme.SPACE_S));
            list.add(row);
        }
        if (entries.isEmpty()) {
            JLabel none = new JLabel("No results recorded yet.");
            none.setFont(Theme.body(Theme.SIZE_M));
            none.setForeground(Theme.INK_MUTED);
            list.add(none);
        }
        fillUndo(entries.isEmpty() ? null : entries.get(0));
        list.revalidate();
        list.repaint();
    }

    /** The Next undo tile: which result, what changes, and whether Undo is allowed right now. */
    private void fillUndo(ResultStack.Entry top) {
        effects.removeAll();
        undoBody.remove(empty);
        if (top == null) {
            undoBody.getComponent(0).setVisible(false);
            undoBody.add(empty, BorderLayout.CENTER);
            return;
        }
        undoBody.getComponent(0).setVisible(true);
        Match m = top.match;
        String id = Tournament.matchId(m);
        undoMeta.setText(id + " · " + tournament.roundName(m, false));
        undoWinner.setText(top.winner.name);
        undoRest.setText(" beat " + top.loser.name);
        effects.add(effect(top.winner.name + " loses the win and 1 point."));
        effects.add(effect(top.loser.name + " loses the loss."));
        boolean blocked = false;
        if (m.next != null) {
            String next = Tournament.matchId(m.next);
            blocked = m.next.status == Match.Status.IN_PROGRESS || m.next.status == Match.Status.PLAYED;
            effects.add(effect(blocked
                    ? next + " has already started, so this result can't be undone until " + next + " is finished or undone."
                    : top.winner.name + " leaves the " + next + " slot, so " + next + " waits again."));
        } else {
            effects.add(effect(m.bracket ? "The champion is cleared." : "The extra match is open again."));
        }
        undoButton.setText("Undo " + id);
        undoButton.setEnabled(!blocked);
        undoButton.getAccessibleContext().setAccessibleDescription(blocked ? "Blocked while the next match is in progress" : null);
        effects.revalidate();
    }

    private static JComponent effect(String text) {
        JLabel l = new JLabel("<html>•&nbsp;&nbsp;" + text + "</html>");
        l.setFont(Theme.body(Theme.SIZE_M));
        l.setForeground(Theme.INK);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SPACE_S, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    /** One result. The newest is tinted amber, outlined, and tagged "Top". */
    private static final class Row extends JPanel {
        private final boolean top;

        Row(int step, ResultStack.Entry e, String round, boolean top) {
            super(new BorderLayout(Theme.SPACE_L, 0));
            this.top = top;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_M, Theme.SPACE_L, Theme.SPACE_M, Theme.SPACE_L));

            JLabel number = new JLabel(String.valueOf(step));
            number.setFont(Theme.display(Theme.SIZE_L));
            number.setForeground(top ? Theme.PRIMARY : Theme.INK);
            number.setPreferredSize(new Dimension(36, number.getPreferredSize().height));
            JLabel id = new JLabel(Tournament.matchId(e.match));
            id.setFont(Theme.body(Theme.SIZE_S));
            id.setForeground(Theme.INK_MUTED);
            id.setPreferredSize(new Dimension(60, id.getPreferredSize().height));
            JLabel winner = new JLabel(e.winner.name);
            winner.setFont(Theme.body(Theme.SIZE_M).deriveFont(Font.BOLD));
            winner.setForeground(top ? Theme.PRIMARY : Theme.INK);
            JLabel rest = new JLabel(" beat " + e.loser.name);
            rest.setFont(Theme.body(Theme.SIZE_M));
            rest.setForeground(Theme.INK);
            JPanel lead = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            lead.setOpaque(false);
            lead.add(number);
            lead.add(Box.createHorizontalStrut(Theme.SPACE_M));
            lead.add(id);
            lead.add(Box.createHorizontalStrut(Theme.SPACE_L));
            lead.add(winner);
            lead.add(rest);

            JLabel roundLabel = new JLabel(round);
            roundLabel.setFont(Theme.body(Theme.SIZE_S));
            roundLabel.setForeground(Theme.INK_MUTED);
            JPanel trail = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACE_M, 0));
            trail.setOpaque(false);
            trail.add(roundLabel);
            if (top) trail.add(new TopPill());
            add(lead, BorderLayout.CENTER);
            add(trail, BorderLayout.EAST);
            getAccessibleContext().setAccessibleName("Step " + step + ", " + Tournament.matchId(e.match) + ", "
                    + e.winner.name + " beat " + e.loser.name + ", " + round + (top ? ", next to undo" : ""));
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(top ? Theme.SELECTION : Theme.PANEL_DEEP);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 2 * ROW_RADIUS, 2 * ROW_RADIUS);
            if (top) {
                g2.setColor(Theme.PRIMARY);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 2 * ROW_RADIUS, 2 * ROW_RADIUS);
            }
            g2.dispose();
        }
    }

    /** The small amber "Top" tag on the newest result. */
    private static final class TopPill extends JLabel {
        TopPill() {
            super("Top", SwingConstants.CENTER);
            setFont(Theme.body(Theme.SIZE_S).deriveFont(Font.BOLD));
            setForeground(Theme.ON_PRIMARY);
            setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_XS, Theme.SPACE_M, Theme.SPACE_XS, Theme.SPACE_M));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PRIMARY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Inner card on the tile (PANEL_DEEP, radius 16). */
    private static final class Inner extends JPanel {
        Inner() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PANEL_DEEP);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
            g2.dispose();
        }
    }
}
