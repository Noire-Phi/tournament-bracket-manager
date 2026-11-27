package ui;

import algo.Rounds;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import ds.ResultStack;
import model.Match;
import model.Player;
import model.Tournament;

/**
 * Dashboard (UI_GUIDE section 5): row 1 is the amber "Now playing" hero tile (2 parts) and the Round tile (1 part),
 * row 2 is Up next, Top of the table and Just happened. Under 1100px wide the tiles stack in one column.
 */
public class DashboardPanel extends Page {
    private static final int RISE = 14;               // entrance and champion rise, px
    private static final int SLIDE = 18;              // Start Next and Undo slide, px
    private static final int ONE_COLUMN_BELOW = 1100; // frame width, UI_GUIDE section 5
    private static final int ROW1_H = 340; // minimum heights; the 390:300 weights share any extra room
    private static final int ROW2_H = 262;
    private static final int TILE_GAP = 18;
    private static final int SEGMENT_FILL_MS = 600;
    private static final int TABLE_ROWS = 4;
    private static final int RESULT_ROWS = 3;
    private static final int UP_NEXT_CARDS = 2;

    private final JPanel grid = new JPanel();
    private final Reveal[] tiles = new Reveal[5];
    private boolean narrow;

    // Hero tile
    private final Card hero = new Card("Now playing");
    private final CardLayout heroSwap = new CardLayout();
    private final JPanel heroBody = new JPanel(heroSwap);
    private final DashboardParts.TagPill heroTag = new DashboardParts.TagPill();
    private final DashboardParts.Versus versus = new DashboardParts.Versus();
    private final JLabel heroBig = new JLabel();
    private final JLabel heroSmall = new JLabel();
    private final Reveal heroBlock = new Reveal(heroBody);
    private Match heroMatch;

    // Round tile
    private final DashboardParts.BigNumber bigNumber = new DashboardParts.BigNumber();
    private final JLabel ofLabel = new JLabel();
    private final DashboardParts.SegmentBar segments = new DashboardParts.SegmentBar();
    private final JLabel roundCaption = new JLabel();
    private final Reveal roundBlock;

    // Row 2 tiles
    private final JPanel upList = column();
    private final Reveal upBlock = new Reveal(upList);
    private final JPanel tableList = column();
    private final Reveal tableBlock = new Reveal(tableList);
    private final JPanel justList = column();
    private final Reveal justBlock = new Reveal(justList);

    private String heroSig = "", roundSig = "", upSig = "", tableSig = "", justSig = "";
    private boolean championShown;

    public DashboardPanel(Tournament tournament, Host host) {
        super(tournament, host);
        setLayout(new BorderLayout());

        buildHero();
        roundBlock = new Reveal(buildRound());
        Card round = new Card("Round");
        round.body().add(roundBlock, BorderLayout.CENTER);
        Card up = new Card("Up next");
        up.body().add(upBlock, BorderLayout.NORTH);
        Card table = new Card("Top of the table");
        table.body().add(tableBlock, BorderLayout.NORTH);
        Card just = new Card("Just happened");
        just.body().add(justBlock, BorderLayout.NORTH);
        StyledButton undo = new StyledButton("Undo last result", StyledButton.Kind.DANGER);
        undo.addActionListener(e -> undo());
        JPanel undoRow = StyledButton.row(undo);
        just.body().add(undoRow, BorderLayout.SOUTH);

        Card[] cards = {hero, round, up, table, just};
        for (int i = 0; i < cards.length; i++) tiles[i] = new Reveal(cards[i]);
        grid.setOpaque(false);
        layoutTiles();

        ScrollContent content = new ScrollContent();
        content.add(grid, BorderLayout.CENTER);
        add(ScrollContent.pane(content), BorderLayout.CENTER);
        refresh();
    }

    // ----- construction -----

    private void buildHero() {
        hero.setBackground(Theme.PRIMARY);
        hero.setTitleColor(Theme.ON_PRIMARY);
        versus.setColors(Theme.ON_PRIMARY, Theme.BG, Theme.PRIMARY);

        StyledButton enter = new StyledButton("Enter result");
        StyledButton start = new StyledButton("Start next match");
        enter.putClientProperty(StyledButton.ON_ACCENT, Boolean.TRUE);
        start.putClientProperty(StyledButton.ON_ACCENT, Boolean.TRUE);
        enter.addActionListener(e -> enterResult());
        start.addActionListener(e -> startNext());
        JPanel buttons = StyledButton.row(enter, start);

        JPanel match = new JPanel(new BorderLayout(0, Theme.SPACE_M));
        match.setOpaque(false);
        JPanel tagRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        tagRow.setOpaque(false);
        tagRow.add(heroTag);
        match.add(tagRow, BorderLayout.NORTH);
        match.add(versus, BorderLayout.CENTER);
        match.add(buttons, BorderLayout.SOUTH);

        heroBig.setFont(Theme.display(Theme.SIZE_XXL));
        heroBig.setForeground(Theme.ON_PRIMARY);
        heroSmall.setFont(Theme.body(Theme.SIZE_M));
        heroSmall.setForeground(Theme.ON_PRIMARY);
        JPanel message = new JPanel(new BorderLayout(0, Theme.SPACE_S));
        message.setOpaque(false);
        message.add(heroBig, BorderLayout.NORTH);
        message.add(heroSmall, BorderLayout.CENTER);

        heroBody.setOpaque(false);
        heroBody.add(match, "match");
        heroBody.add(message, "message");
        hero.body().add(heroBlock, BorderLayout.CENTER);
    }

    private JPanel buildRound() {
        ofLabel.setFont(Theme.display(Theme.SIZE_XXL));
        ofLabel.setForeground(Theme.INK);
        JPanel ofWrap = new JPanel(new BorderLayout());
        ofWrap.setOpaque(false);
        ofWrap.setBorder(BorderFactory.createEmptyBorder(0, Theme.SPACE_S, 0, 0));
        ofWrap.add(ofLabel, BorderLayout.SOUTH);
        JPanel number = new JPanel(new BorderLayout());
        number.setOpaque(false);
        number.add(bigNumber, BorderLayout.WEST);
        number.add(ofWrap, BorderLayout.CENTER);

        roundCaption.setFont(Theme.body(Theme.SIZE_M));
        roundCaption.setForeground(Theme.INK_MUTED);
        JPanel foot = new JPanel(new BorderLayout(0, Theme.SPACE_M));
        foot.setOpaque(false);
        foot.add(segments, BorderLayout.NORTH);
        foot.add(roundCaption, BorderLayout.SOUTH);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(number, BorderLayout.NORTH);
        p.add(foot, BorderLayout.SOUTH);
        return p;
    }

    private static JPanel column() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    /** Wide: hero + Round over three equal tiles. Narrow: one column. */
    private void layoutTiles() {
        grid.removeAll();
        for (int i = 0; i < tiles.length; i++) {
            tiles[i].setPreferredSize(new Dimension(0, i < 2 ? ROW1_H : ROW2_H));
        }
        if (narrow) {
            grid.setLayout(new GridLayout(0, 1, 0, TILE_GAP));
            for (Reveal t : tiles) grid.add(t);
        } else {
            JPanel row1 = new JPanel(new GridBagLayout());
            row1.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1;
            c.weightx = 2;
            c.insets = new java.awt.Insets(0, 0, 0, TILE_GAP);
            row1.add(tiles[0], c);
            c.weightx = 1;
            c.insets = new java.awt.Insets(0, 0, 0, 0);
            row1.add(tiles[1], c);
            JPanel row2 = new JPanel(new GridLayout(1, 3, TILE_GAP, 0));
            row2.setOpaque(false);
            row2.add(tiles[2]);
            row2.add(tiles[3]);
            row2.add(tiles[4]);

            grid.setLayout(new GridBagLayout());
            GridBagConstraints r = new GridBagConstraints();
            r.gridx = 0;
            r.fill = GridBagConstraints.BOTH;
            r.weightx = 1;
            r.weighty = 390;
            r.insets = new java.awt.Insets(0, 0, TILE_GAP, 0);
            grid.add(row1, r);
            r.weighty = 300;
            r.insets = new java.awt.Insets(0, 0, 0, 0);
            grid.add(row2, r);
        }
        grid.revalidate();
        grid.repaint();
    }

    @Override
    public void resized(int frameWidth) {
        boolean one = frameWidth < ONE_COLUMN_BELOW;
        if (one == narrow) return;
        narrow = one;
        layoutTiles();
    }

    // ----- actions -----

    /** Asks who won the match in the hero tile and records it. */
    private void enterResult() {
        Match m = heroMatch;
        if (m == null || m.a == null || m.b == null) {
            host.notify("No match to enter a result for.", true);
            return;
        }
        int choice = Confirm.choose(this, "Enter result", "Who won " + Tournament.matchId(m) + "?", m.a.name, m.b.name);
        if (choice < 0) return;
        Player winner = choice == 0 ? m.a : m.b;
        try {
            tournament.setWinner(m, winner);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            host.notify(ex.getMessage() + ".", true);
            return;
        }
        String message = Tournament.matchId(m) + ": " + winner.name + " wins";
        if (m.next != null) message += " and moves on to " + Tournament.matchId(m.next);
        else if (tournament.champion() == winner) message += ". Champion";
        host.notify(message + ".", false);
        render(true);
    }

    private void startNext() {
        if (StartNextAction.run(tournament, host)) render(true);
    }

    private void undo() {
        if (UndoAction.run(this, tournament, host)) render(true);
    }

    // ----- drawing the data -----

    @Override
    public String title() {
        return "Dashboard";
    }

    @Override
    public void refresh() {
        render(false);
    }

    /** Entrance: tiles rise in 80ms apart; the round number scales in, the bar fills, the names slide in. */
    @Override
    public void opened() {
        for (int i = 0; i < tiles.length; i++) tiles[i].play(RISE, Theme.MOTION_SLOW_MS, 80 * i);
        bigNumber.slam(160);
        segments.fill(240, SEGMENT_FILL_MS);
        if (heroMatch != null) versus.play(240, Theme.MOTION_SLOW_MS);
    }

    /** Redraws everything. With animate, only the blocks whose content changed slide in. */
    private void render(boolean animate) {
        String newHero = fillHero();
        String newRound = fillRound();
        String newUp = fillUpNext();
        String newTable = fillTable();
        String newJust = fillJust();

        boolean championNow = tournament.champion() != null;
        if (championNow && !championShown) {
            heroBlock.play(RISE, Theme.MOTION_SLOW_MS, 0); // the champion rises and settles, once
        } else if (animate && !newHero.equals(heroSig)) {
            heroBlock.play(SLIDE, Theme.MOTION_MS, 0);
            if (heroMatch != null) versus.play(0, Theme.MOTION_MS);
        }
        championShown = championNow;
        if (animate) {
            if (!newRound.equals(roundSig)) roundBlock.play(SLIDE, Theme.MOTION_MS, 0);
            if (!newUp.equals(upSig)) upBlock.play(SLIDE, Theme.MOTION_MS, 0);
            if (!newTable.equals(tableSig)) tableBlock.play(SLIDE, Theme.MOTION_MS, 0);
            if (!newJust.equals(justSig)) justBlock.play(SLIDE, Theme.MOTION_MS, 0);
        }
        heroSig = newHero;
        roundSig = newRound;
        upSig = newUp;
        tableSig = newTable;
        justSig = newJust;
        revalidate();
        repaint();
    }

    private int totalRounds() {
        return tournament.isGenerated() ? Rounds.rounds(tournament.players().size()) : 0;
    }

    private String roundName(Match m) {
        return tournament.roundName(m, false);
    }

    private static String record(Player p) {
        return p.wins + (p.wins == 1 ? " win" : " wins") + " · " + p.losses + (p.losses == 1 ? " loss" : " losses");
    }

    private String fillHero() {
        Player champ = tournament.champion();
        Match playing = tournament.inProgress();
        heroMatch = playing != null ? playing : tournament.nextMatch();

        if (champ != null) {
            heroMatch = null;
            hero.setTitle("Champion");
            heroBig.setText(champ.name);
            heroSmall.setText(record(champ) + " · " + tournament.name);
            heroSwap.show(heroBody, "message");
            return "champion " + champ.id;
        }
        if (heroMatch == null) {
            hero.setTitle("Now playing");
            heroBig.setText(tournament.isGenerated() ? "No match ready" : "No bracket yet");
            heroSmall.setText(tournament.isGenerated() ? "Enter the results that are still open on the Matches page."
                    : "Add 4, 8 or 16 " + tournament.sport.entity.toLowerCase() + "s, then generate the bracket.");
            heroSwap.show(heroBody, "message");
            return "none " + tournament.isGenerated();
        }
        boolean live = heroMatch.status == Match.Status.IN_PROGRESS;
        hero.setTitle(live ? "Now playing" : "Next match");
        heroTag.set((live ? "Playing now · " : "Ready · ") + Tournament.matchId(heroMatch) + " · " + roundName(heroMatch), live);
        versus.setNames(heroMatch.a.name, heroMatch.b.name, record(heroMatch.a), record(heroMatch.b));
        heroSwap.show(heroBody, "match");
        return heroMatch.id + " " + heroMatch.status;
    }

    private String fillRound() {
        int total = totalRounds();
        int[] done = new int[total + 1];
        int[] count = new int[total + 1];
        int played = 0, matches = 0;
        for (Match m : tournament.matches().list()) {
            if (!m.bracket) continue;
            count[m.round]++;
            matches++;
            if (m.status == Match.Status.PLAYED) {
                done[m.round]++;
                played++;
            }
        }
        int roundsDone = 0;
        for (int r = 1; r <= total; r++) {
            if (count[r] > 0 && done[r] == count[r]) roundsDone++;
        }
        if (total == 0) {
            bigNumber.setText("-");
            ofLabel.setText("");
            segments.set(0, 0, -1);
            roundCaption.setText("No bracket yet.");
            return "none";
        }
        boolean finished = tournament.champion() != null;
        int current = finished ? total : tournament.currentRound();
        bigNumber.setText(String.valueOf(current));
        ofLabel.setText("of " + total);
        segments.set(total, roundsDone, finished ? -1 : current - 1);
        String name = finished ? "Complete" : tournament.roundName(current, true);
        roundCaption.setText(name + " · " + played + " of " + matches + " matches played");
        return current + "/" + roundsDone + "/" + played;
    }

    private String fillUpNext() {
        upList.removeAll();
        StringBuilder sig = new StringBuilder();
        int shown = 0;
        for (Match m : tournament.ready().list()) {
            if (m == heroMatch || shown == UP_NEXT_CARDS) continue;
            addCard(new DashboardParts.InnerCard(Tournament.matchId(m) + " · Ready", m.a.name + " vs " + m.b.name, false));
            sig.append(m.id).append(' ');
            shown++;
        }
        for (Match m : tournament.matches().list()) {
            if (shown == UP_NEXT_CARDS) break;
            if (!m.bracket || m.status != Match.Status.WAITING) continue;
            Player known = m.a != null ? m.a : m.b;
            String text = known == null ? "Waits for both winners" : "Waits for " + known.name + "'s opponent";
            addCard(new DashboardParts.InnerCard(Tournament.matchId(m) + " · " + roundName(m), text, true));
            sig.append('w').append(m.id).append(' ');
            shown++;
        }
        if (shown == 0) upList.add(note(tournament.isGenerated() ? "Nothing waiting." : "No bracket yet."));
        return sig.toString();
    }

    private void addCard(DashboardParts.InnerCard card) {
        card.setAlignmentX(LEFT_ALIGNMENT);
        if (upList.getComponentCount() > 0) upList.add(Box.createVerticalStrut(Theme.SPACE_M));
        upList.add(card);
    }

    private String fillTable() {
        tableList.removeAll();
        List<Player> standings = tournament.standings();
        int[] ranks = algo.Ranks.of(standings);
        StringBuilder sig = new StringBuilder();
        for (int i = 0; i < Math.min(TABLE_ROWS, standings.size()); i++) {
            Player p = standings.get(i);
            JLabel name = new JLabel(p.name);
            name.setFont(Theme.body(Theme.SIZE_M));
            name.setForeground(Theme.INK);
            name.setBorder(BorderFactory.createEmptyBorder(0, Theme.SPACE_M, 0, 0));
            JLabel pts = new JLabel(p.points + (p.points == 1 ? " pt" : " pts"));
            pts.setFont(Theme.body(Theme.SIZE_M).deriveFont(Font.BOLD));
            pts.setForeground(Theme.INK);
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_XS, 0, Theme.SPACE_XS, 0));
            row.add(new Chip(ranks[i], algo.Ranks.group(standings, i)), BorderLayout.WEST);
            row.add(name, BorderLayout.CENTER);
            row.add(pts, BorderLayout.EAST);
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            row.getAccessibleContext().setAccessibleName("Rank " + ranks[i] + ", " + p.name + ", " + p.points + " points");
            tableList.add(row);
            sig.append(p.id).append(':').append(p.points).append(' ');
        }
        if (standings.isEmpty()) tableList.add(note("No " + tournament.sport.entity.toLowerCase() + "s yet."));
        return sig.toString();
    }

    private String fillJust() {
        justList.removeAll();
        List<ResultStack.Entry> recent = tournament.results().newestFirst();
        StringBuilder sig = new StringBuilder();
        for (int i = 0; i < Math.min(RESULT_ROWS, recent.size()); i++) {
            ResultStack.Entry e = recent.get(i);
            JLabel winner = new JLabel(e.winner.name);
            winner.setFont(Theme.body(Theme.SIZE_M).deriveFont(Font.BOLD));
            winner.setForeground(Theme.INK);
            JLabel rest = new JLabel(" beat " + e.loser.name);
            rest.setFont(Theme.body(Theme.SIZE_M));
            rest.setForeground(Theme.INK);
            JLabel id = new JLabel(Tournament.matchId(e.match));
            id.setFont(Theme.body(Theme.SIZE_S));
            id.setForeground(Theme.INK_MUTED);
            JPanel text = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            text.setOpaque(false);
            text.add(winner);
            text.add(rest);
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, Theme.BORDER, 0, Theme.LINE),
                    BorderFactory.createEmptyBorder(Theme.SPACE_M, 0, Theme.SPACE_M, 0)));
            row.add(text, BorderLayout.CENTER);
            row.add(id, BorderLayout.EAST);
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            justList.add(row);
            sig.append(e.match.id).append(' ');
        }
        if (recent.isEmpty()) justList.add(note("No results yet."));
        justList.getAccessibleContext().setAccessibleName("Latest results, newest first");
        return sig.toString();
    }

    private static JLabel note(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.body(Theme.SIZE_S));
        l.setForeground(Theme.INK_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    /** A rank chip as a component, for the Top of the table rows. */
    private static final class Chip extends JComponent {
        private final int rank, group;

        Chip(int rank, int group) {
            this.rank = rank;
            this.group = group;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(RankChip.SIZE, RankChip.SIZE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            RankChip.paint(g2, 0, (getHeight() - RankChip.SIZE) / 2, rank, group);
            g2.dispose();
        }
    }
}
