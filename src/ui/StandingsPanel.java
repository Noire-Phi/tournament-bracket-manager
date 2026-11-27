package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Match;
import model.Player;
import model.Tournament;

/**
 * Standings, view only: three counts (still in, knocked out, matches played) above the ranked table.
 * Sorted by points with ties by name; tied players share a rank; leaders get chips.
 */
public class StandingsPanel extends Page {
    private static final String STILL_IN = "Still in";
    private static final String OUT = "Out";

    private final PageHeader header = new PageHeader();
    private final Kpi stillIn = new Kpi(true);
    private final Kpi knockedOut = new Kpi(false);
    private final Kpi played = new Kpi(false);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Rank", "Name", "Bracket", "Points", "Wins", "Losses"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column <= 2 ? String.class : Integer.class; // Rank is a chip at the left, so its header is left too
        }
    };
    private final JTable table = new JTable(model);
    private List<Player> shown = new ArrayList<>();
    private int[] ranks = new int[0];
    private Player champion;

    public StandingsPanel(Tournament tournament, Host host) {
        super(tournament, host);
        setLayout(new BorderLayout());

        TableStyle.apply(table);
        Cell cell = new Cell();
        table.setDefaultRenderer(Object.class, cell);
        table.setDefaultRenderer(Number.class, cell);
        int[] widths = {90, 260, 160, 110, 110, 110};
        for (int col = 0; col < widths.length; col++) table.getColumnModel().getColumn(col).setPreferredWidth(widths[col]);
        table.getAccessibleContext().setAccessibleName("Standings table");

        JPanel kpis = new JPanel(new GridLayout(1, 3, Theme.SPACE_L, 0));
        kpis.setOpaque(false);
        kpis.add(stillIn);
        kpis.add(knockedOut);
        kpis.add(played);

        Card tableCard = new Card("");
        tableCard.body().add(TableStyle.box(TableStyle.scroll(table)), BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, Theme.SPACE_XL));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SPACE_L, 0));
        top.add(header, BorderLayout.NORTH);
        top.add(kpis, BorderLayout.CENTER);

        ScrollContent content = new ScrollContent();
        content.add(top, BorderLayout.NORTH);
        content.add(tableCard, BorderLayout.CENTER);
        add(ScrollContent.pane(content), BorderLayout.CENTER);
        refresh();
    }

    @Override
    public String title() {
        return "Standings";
    }

    @Override
    public void refresh() {
        String plural = tournament.sport.entity.toLowerCase() + "s";
        header.set(title(), "View only · tied " + plural + " share a rank and are listed by name");
        champion = tournament.champion();
        shown = tournament.standings(); // selection sort by points, insertion sort by name for ties
        ranks = algo.Ranks.of(shown);
        model.setRowCount(0);
        int out = 0;
        for (int i = 0; i < shown.size(); i++) {
            Player p = shown.get(i);
            boolean isOut = tournament.isGenerated() && tournament.isOut(p);
            if (isOut) out++;
            String bracket = !tournament.isGenerated() ? "-" : isOut ? OUT : STILL_IN;
            model.addRow(new Object[] {ranks[i], p.name, bracket, p.points, p.wins, p.losses});
        }
        int done = 0, total = 0;
        for (Match m : tournament.matches().list()) {
            if (!m.bracket) continue;
            total++;
            if (m.status == Match.Status.PLAYED) done++;
        }
        boolean started = tournament.isGenerated();
        stillIn.set(started ? String.valueOf(shown.size() - out) : "-", "", plural + " still in");
        knockedOut.set(started ? String.valueOf(out) : "-", "", plural + " knocked out");
        played.set(String.valueOf(done), started ? "/ " + total : "", "matches played");
    }

    /** A count tile: big number (with an optional smaller "of" part) over a label. The first one is amber. */
    private static final class Kpi extends Card {
        private final JLabel value = new JLabel();
        private final JLabel of = new JLabel();
        private final JLabel label = new JLabel();

        Kpi(boolean accent) {
            super("");
            if (accent) setBackground(Theme.PRIMARY);
            value.setFont(Theme.display(Theme.SIZE_XXL));
            value.setForeground(accent ? Theme.ON_PRIMARY : Theme.INK);
            of.setFont(Theme.display(Theme.SIZE_XL));
            of.setForeground(accent ? Theme.ON_PRIMARY : Theme.INK_MUTED);
            label.setFont(Theme.body(Theme.SIZE_M).deriveFont(accent ? Font.BOLD : Font.PLAIN));
            label.setForeground(accent ? Theme.ON_PRIMARY : Theme.INK_MUTED);
            JPanel number = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_S, 0));
            ((FlowLayout) number.getLayout()).setAlignOnBaseline(true);
            number.setOpaque(false);
            number.add(value);
            number.add(of);
            JPanel stack = new JPanel(new BorderLayout(0, Theme.SPACE_XS));
            stack.setOpaque(false);
            stack.add(number, BorderLayout.CENTER);
            stack.add(label, BorderLayout.SOUTH);
            body().add(stack, BorderLayout.CENTER);
            setPreferredSize(new Dimension(0, 150));
        }

        void set(String number, String ofText, String text) {
            value.setText(number);
            of.setText(ofText);
            label.setText(text);
            getAccessibleContext().setAccessibleName(number + " " + ofText + " " + text);
        }
    }

    /** Rank chip in the first column, a Champion pill after the winner's name, amber "Still in", bold points. */
    private final class Cell extends DefaultTableCellRenderer {
        private static final int PILL_ARC = 999;
        private boolean selected, first, pill;
        private int row;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(t, column == 0 ? "" : value, isSelected, false, row, column);
            this.row = row;
            selected = isSelected;
            first = column == 0;
            pill = column == 1 && row < shown.size() && shown.get(row) == champion;
            setBackground(isSelected ? Theme.SELECTION : Theme.PANEL_DEEP);
            setForeground(column == 2 ? (STILL_IN.equals(value) ? Theme.PRIMARY : Theme.INK_MUTED) : Theme.INK);
            boolean bold = column == 1 || column == 2 || column == 3;
            setFont(column == 3 ? Theme.display(Theme.SIZE_L)
                    : Theme.body(Theme.SIZE_M).deriveFont(bold ? Font.BOLD : Font.PLAIN));
            setHorizontalAlignment(value instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, Theme.SPACE_L, 0, Theme.SPACE_L));
            if (first && row < ranks.length) {
                getAccessibleContext().setAccessibleName("Rank " + ranks[row]);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (first && row < ranks.length) {
                RankChip.paint(g2, Theme.SPACE_L, (getHeight() - RankChip.SIZE) / 2, ranks[row],
                        algo.Ranks.group(shown, row));
                if (selected) {
                    g2.setColor(Theme.PRIMARY);
                    g2.fillRect(0, 0, Theme.SELECTION_BAR, getHeight());
                }
            }
            if (pill) {
                int textEnd = Theme.SPACE_L + g2.getFontMetrics(getFont()).stringWidth(getText());
                g2.setFont(Theme.body(Theme.SIZE_S).deriveFont(Font.BOLD));
                FontMetrics fm = g2.getFontMetrics();
                String word = "Champion";
                int w = fm.stringWidth(word) + 2 * Theme.SPACE_M;
                int h = fm.getHeight() + Theme.SPACE_XS;
                int x = textEnd + Theme.SPACE_M;
                int y = (getHeight() - h) / 2;
                g2.setColor(Theme.GOLD);
                g2.fillRoundRect(x, y, w, h, PILL_ARC, PILL_ARC);
                g2.setColor(Theme.ON_PRIMARY);
                g2.drawString(word, x + Theme.SPACE_M, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }
}
