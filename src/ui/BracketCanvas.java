package ui;

import algo.Rounds;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import model.Match;
import model.Player;
import model.Tournament;

/**
 * The bracket, painted by one component. Everything paintComponent needs (positions, strings, widths)
 * is worked out in sync(), so painting only reads arrays and draws: no objects are created per frame.
 *
 * States are shown by text and border, never colour alone: played (winner tinted with a check, loser struck
 * through), in progress (red border, LIVE), ready (white border, READY), waiting (dashed border, "Winner of M013").
 */
final class BracketCanvas extends JPanel {
    static final int CARD_W = 196;
    static final int CARD_H = 64;
    private static final int COL_GAP = 40;       // 20px stub out of a card, 20px stub into the next
    private static final int ROW_GAP = 16;
    private static final int PAD = 8;
    private static final int HEADER_H = 40;
    private static final int ROW_H = CARD_H / 2;
    private static final int TEXT_PAD = 14;
    private static final int ARC = 16;
    private static final int CHAMP_H = 132;
    private static final int GLYPH = 16;
    private static final int TROPHY = 36;
    private static final int SLIDE_PX = 18;
    private static final float FADE_FROM = 0.35f;
    private static final int ALPHA_STEPS = 16;
    private static final String ELLIPSIS = "…";
    private static final String LIVE_TAG = "LIVE";
    private static final String READY_TAG = "READY";

    private static final Font NAME_FONT = Theme.body(Theme.SIZE_M).deriveFont(Font.BOLD);
    private static final Font WAIT_FONT = Theme.body(Theme.SIZE_S).deriveFont(Font.ITALIC);
    private static final Font TAG_FONT = Theme.body(Theme.SIZE_S).deriveFont(Font.BOLD);
    private static final Font HEAD_FONT = Theme.display(Theme.SIZE_M);
    private static final Font CHAMP_FONT = Theme.display(Theme.SIZE_L);
    private static final Font KICKER_FONT = Theme.body(Theme.SIZE_S).deriveFont(Font.BOLD);
    private static final Font EMPTY_TITLE_FONT = Theme.display(Theme.SIZE_L);
    private static final Font EMPTY_TEXT_FONT = Theme.body(Theme.SIZE_M);
    private static final Stroke ONE = new BasicStroke(1f);
    private static final Stroke TWO = new BasicStroke(2f);
    private static final Stroke DASH = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
            10f, new float[] {6f, 5f}, 0f);
    private static final Stroke GLYPH_STROKE = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Path2D CHECK_PATH = new Path2D.Float();
    private static final Path2D TROPHY_PATH = new Path2D.Float();
    private static final AlphaComposite[] ALPHAS = new AlphaComposite[ALPHA_STEPS + 1];

    static {
        for (int i = 0; i <= ALPHA_STEPS; i++) ALPHAS[i] = AlphaComposite.SrcOver.derive(i / (float) ALPHA_STEPS);
        // Both glyphs on a 24-unit grid, built once.
        CHECK_PATH.moveTo(5, 12.5);
        CHECK_PATH.lineTo(10, 17.5);
        CHECK_PATH.lineTo(19, 7);
        TROPHY_PATH.moveTo(7, 4);
        TROPHY_PATH.lineTo(17, 4);
        TROPHY_PATH.lineTo(17, 9);
        TROPHY_PATH.curveTo(17, 12.5, 14.8, 15, 12, 15);
        TROPHY_PATH.curveTo(9.2, 15, 7, 12.5, 7, 9);
        TROPHY_PATH.closePath();
        TROPHY_PATH.moveTo(7, 6);
        TROPHY_PATH.lineTo(4, 6);
        TROPHY_PATH.curveTo(4, 9.5, 5.5, 11, 7.6, 11.4);
        TROPHY_PATH.moveTo(17, 6);
        TROPHY_PATH.lineTo(20, 6);
        TROPHY_PATH.curveTo(20, 9.5, 18.5, 11, 16.4, 11.4);
        TROPHY_PATH.moveTo(12, 15);
        TROPHY_PATH.lineTo(12, 18);
        TROPHY_PATH.moveTo(9, 18);
        TROPHY_PATH.lineTo(15, 18);
        TROPHY_PATH.moveTo(8, 20);
        TROPHY_PATH.lineTo(16, 20);
    }

    private final Tournament tournament;

    // Everything below is rebuilt by sync() and only read by paintComponent().
    private int count;
    private int rounds;
    private int currentRound;
    private Match[] matches = new Match[0];
    private boolean[] wasPlayed = new boolean[0];
    private int[] px = new int[0];
    private int[] py = new int[0];
    private int[] parent = new int[0];
    private int[] winnerRow = new int[0];       // 0 none, 1 first row, 2 second row
    private Match.Status[] status = new Match.Status[0];
    private String[][] names = new String[0][];
    private int[][] nameWidth = new int[0][];
    private boolean[][] waiting = new boolean[0][];
    private float[] fade = new float[0];
    private int[] slide = new int[0];
    private String[] roundTitle = new String[0];
    private String emptyHint = "";
    private String champText = "";
    private boolean champDecided;
    private int champX, champY, champTextX, champKickerX;
    private int nameBase, waitBase, tagBase, liveWidth, readyWidth;

    BracketCanvas(Tournament tournament) {
        this.tournament = tournament;
        setBackground(Theme.PANEL);
        setOpaque(true);
        getAccessibleContext().setAccessibleName("Bracket");
        // Explicitly focusable so Tab reaches it; the arrow keys then scroll it (JScrollPane binds them).
        setFocusable(true);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    /** Re-reads the tournament. Cards that were decided since the last call fade and slide in. */
    void sync() {
        boolean wasEmpty = count == 0;
        Match[] before = matches;
        boolean[] playedBefore = wasPlayed;

        List<Match> list = new ArrayList<>();
        for (Match m : tournament.matches().list()) {
            if (m.bracket) list.add(m);
        }
        count = list.size();
        emptyHint = "Add 4, 8 or 16 " + tournament.sport.entity.toLowerCase() + "s, then press Generate bracket.";
        rounds = count == 0 ? 0 : Rounds.rounds(tournament.players().size());
        Player champion = tournament.champion();
        currentRound = champion != null ? rounds + 1 : tournament.currentRound();
        matches = list.toArray(new Match[0]);
        wasPlayed = new boolean[count];
        px = new int[count];
        py = new int[count];
        parent = new int[count];
        winnerRow = new int[count];
        status = new Match.Status[count];
        names = new String[count][2];
        nameWidth = new int[count][2];
        waiting = new boolean[count][2];
        fade = new float[count];
        slide = new int[count];
        roundTitle = new String[rounds + 1];
        for (int r = 1; r <= rounds; r++) roundTitle[r - 1] = tournament.roundName(r, true);
        roundTitle[rounds] = "Champion";

        FontMetrics nameFm = getFontMetrics(NAME_FONT);
        FontMetrics waitFm = getFontMetrics(WAIT_FONT);
        FontMetrics tagFm = getFontMetrics(TAG_FONT);
        nameBase = (ROW_H - nameFm.getHeight()) / 2 + nameFm.getAscent();
        waitBase = (ROW_H - waitFm.getHeight()) / 2 + waitFm.getAscent();
        tagBase = (ROW_H - tagFm.getHeight()) / 2 + tagFm.getAscent();
        liveWidth = tagFm.stringWidth(LIVE_TAG);
        readyWidth = tagFm.stringWidth(READY_TAG);
        int maxName = CARD_W - 2 * TEXT_PAD - Math.max(readyWidth, GLYPH) - Theme.SPACE_S;

        for (int i = 0; i < count; i++) {
            parent[i] = -1;
            for (int j = 0; j < count; j++) {
                if (matches[i].next == matches[j]) parent[i] = j;
            }
        }
        int[] seen = new int[rounds + 1];
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < count; i++) {
            Match m = matches[i];
            int slot = seen[m.round]++;
            px[i] = PAD + (m.round - 1) * (CARD_W + COL_GAP);
            int pitch = (CARD_H + ROW_GAP) << (m.round - 1);
            py[i] = PAD + HEADER_H + (int) ((slot + 0.5) * pitch) - CARD_H / 2;
            status[i] = m.status;
            wasPlayed[i] = m.status == Match.Status.PLAYED;
            winnerRow[i] = m.winner == null ? 0 : (m.winner == m.a ? 1 : 2);
            for (int row = 0; row < 2; row++) {
                Player p = row == 0 ? m.a : m.b;
                waiting[i][row] = p == null;
                names[i][row] = p == null ? "Winner of " + feederId(i, row) : fit(p.name, maxName, nameFm);
                nameWidth[i][row] = (p == null ? waitFm : nameFm).stringWidth(names[i][row]);
            }
            fade[i] = 1f;
            summary.append(Tournament.matchId(m)).append(", ").append(tournament.roundName(m, false)).append(": ")
                    .append(names[i][0]).append(" versus ").append(names[i][1])
                    .append(m.winner == null ? ", " + StatusPill.label(m.status) : ", winner " + m.winner.name).append(". ");
        }

        // Champion slot: one more column, level with the final.
        champDecided = champion != null;
        champText = champDecided ? champion.name : "To be decided";
        champX = PAD + rounds * (CARD_W + COL_GAP);
        int finalIndex = count - 1;
        champY = count == 0 ? 0 : py[finalIndex] + CARD_H / 2 - CHAMP_H / 2;
        FontMetrics champFm = getFontMetrics(CHAMP_FONT);
        champText = fit(champText, CARD_W - 2 * TEXT_PAD, champFm);
        champTextX = champX + (CARD_W - champFm.stringWidth(champText)) / 2;
        champKickerX = champX + (CARD_W - getFontMetrics(KICKER_FONT).stringWidth("Champion")) / 2;
        if (count > 0) summary.append(champDecided ? "Champion: " + champion.name + "." : "Champion to be decided.");
        getAccessibleContext().setAccessibleDescription(count == 0 ? "No bracket yet." : summary.toString());

        int width = count == 0 ? 520 : 2 * PAD + (rounds + 1) * CARD_W + rounds * COL_GAP;
        int height = count == 0 ? 160 : 2 * PAD + HEADER_H + (tournament.players().size() / 2) * (CARD_H + ROW_GAP);
        setPreferredSize(new Dimension(width, height));
        revalidate();
        repaint();

        // Animate only cards that turned PLAYED, and only when the same bracket was already on screen.
        if (!wasEmpty && sameCards(before, matches)) {
            for (int i = 0; i < count; i++) {
                if (wasPlayed[i] && !playedBefore[i]) decided(i);
            }
        }
    }

    /** The match whose winner fills this slot, as "M013". */
    private String feederId(int i, int row) {
        for (int j = 0; j < count; j++) {
            if (parent[j] == i && matches[j].nextSlot == row) return Tournament.matchId(matches[j]);
        }
        return "?";
    }

    private static boolean sameCards(Match[] a, Match[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    /** Fade (from 35%) and 18px slide up into place, via Motion.animate. */
    private void decided(int i) {
        float[] f = fade;
        int[] s = slide;
        Motion.animate(Theme.MOTION_MS, p -> {
            f[i] = FADE_FROM + (1 - FADE_FROM) * (float) p;
            s[i] = (int) Math.round((1 - p) * SLIDE_PX);
            repaint();
        }, null);
    }

    private static String fit(String text, int maxWidth, FontMetrics fm) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        String cut = text;
        while (cut.length() > 1 && fm.stringWidth(cut + ELLIPSIS) > maxWidth) cut = cut.substring(0, cut.length() - 1);
        return cut + ELLIPSIS;
    }

    // ----- painting: reads arrays and draws; creates nothing -----

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        if (count == 0) {
            paintEmpty(g2);
            return;
        }
        paintHeaders(g2);
        for (int i = 0; i < count; i++) paintConnector(g2, i);
        for (int i = 0; i < count; i++) {
            if (g.hitClip(px[i] - 2, py[i] - SLIDE_PX - 2, CARD_W + 4, CARD_H + SLIDE_PX + 4)) paintCard(g2, i);
        }
        paintChampion(g2);
    }

    private void paintEmpty(Graphics2D g2) {
        g2.setFont(EMPTY_TITLE_FONT);
        g2.setColor(Theme.INK);
        g2.drawString("No bracket yet", PAD, PAD + 28);
        g2.setFont(EMPTY_TEXT_FONT);
        g2.setColor(Theme.INK_MUTED);
        g2.drawString(emptyHint, PAD, PAD + 58);
    }

    /** Round names; the round being played (or the champion once decided) is amber. */
    private void paintHeaders(Graphics2D g2) {
        g2.setFont(HEAD_FONT);
        for (int r = 0; r <= rounds; r++) {
            int x = PAD + r * (CARD_W + COL_GAP);
            g2.setColor(r + 1 == currentRound || (r == rounds && champDecided) ? Theme.PRIMARY : Theme.INK_MUTED);
            g2.drawString(roundTitle[r], x, PAD + 20);
        }
    }

    /** 2px LINE elbow from a card to the card its winner plays in next. */
    private void paintConnector(Graphics2D g2, int i) {
        int half = COL_GAP / 2;
        int y = py[i] + CARD_H / 2;
        int xRight = px[i] + CARD_W;
        g2.setColor(Theme.LINE);
        int p = parent[i];
        if (p < 0) { // the final feeds the champion slot
            g2.fillRect(xRight, y - 1, COL_GAP, 2);
            return;
        }
        int yParent = py[p] + CARD_H / 2;
        g2.fillRect(xRight, y - 1, half, 2);
        g2.fillRect(xRight + half - 1, Math.min(y, yParent) - 1, 2, Math.abs(y - yParent) + 2);
        g2.fillRect(xRight + half, yParent - 1, half, 2);
    }

    private void paintCard(Graphics2D g2, int i) {
        boolean animating = fade[i] < 1f;
        Composite saved = null;
        if (animating) {
            saved = g2.getComposite();
            g2.setComposite(ALPHAS[Math.round(fade[i] * ALPHA_STEPS)]);
            g2.translate(0, slide[i]);
        }
        int x = px[i];
        int y = py[i];
        Match.Status s = status[i];
        boolean isWaiting = s == Match.Status.WAITING;

        g2.setColor(isWaiting ? Theme.PANEL : Theme.PANEL_DEEP);
        g2.fillRoundRect(x, y, CARD_W, CARD_H, ARC, ARC);
        for (int row = 0; row < 2; row++) {
            int ry = y + row * ROW_H;
            boolean won = winnerRow[i] == row + 1;
            boolean lost = winnerRow[i] != 0 && !won;
            if (won) {
                g2.setColor(Theme.WIN_TINT);
                // round only the outer corners of the tinted row
                g2.fillRoundRect(x, ry, CARD_W, ROW_H, ARC, ARC);
                g2.fillRect(x, row == 0 ? ry + ROW_H / 2 : ry, CARD_W, ROW_H / 2);
            }
            boolean wait = waiting[i][row];
            g2.setFont(wait ? WAIT_FONT : NAME_FONT);
            g2.setColor(won ? Theme.WIN : (lost || wait) ? Theme.INK_MUTED : Theme.INK);
            g2.drawString(names[i][row], x + TEXT_PAD, ry + (wait ? waitBase : nameBase));
            if (lost) g2.fillRect(x + TEXT_PAD, ry + ROW_H / 2, nameWidth[i][row], 1);
            if (won) glyph(g2, CHECK_PATH, x + CARD_W - TEXT_PAD - GLYPH, ry + (ROW_H - GLYPH) / 2, GLYPH, Theme.WIN);
        }
        g2.setColor(Theme.LINE);
        g2.fillRect(x + 1, y + ROW_H, CARD_W - 2, 1);

        // Border and tag carry the state.
        if (s == Match.Status.IN_PROGRESS || s == Match.Status.READY) {
            boolean live = s == Match.Status.IN_PROGRESS;
            g2.setColor(live ? Theme.LIVE : Theme.READY_FILL);
            g2.setStroke(TWO);
            g2.drawRoundRect(x + 1, y + 1, CARD_W - 2, CARD_H - 2, ARC, ARC);
            g2.setFont(TAG_FONT);
            g2.setColor(live ? Theme.LIVE : Theme.INK_MUTED);
            g2.drawString(live ? LIVE_TAG : READY_TAG, x + CARD_W - TEXT_PAD - (live ? liveWidth : readyWidth), y + tagBase);
        } else {
            g2.setColor(Theme.LINE);
            g2.setStroke(isWaiting ? DASH : ONE);
            g2.drawRoundRect(x, y, CARD_W - 1, CARD_H - 1, ARC, ARC);
        }
        g2.setStroke(ONE);

        if (animating) {
            g2.translate(0, -slide[i]);
            g2.setComposite(saved);
        }
    }

    /** Dashed amber box with the trophy; it fills in amber once the final is decided. */
    private void paintChampion(Graphics2D g2) {
        int x = champX, y = champY;
        if (champDecided) {
            g2.setColor(Theme.PRIMARY);
            g2.fillRoundRect(x, y, CARD_W, CHAMP_H, ARC, ARC);
        } else {
            g2.setColor(Theme.PRIMARY);
            g2.setStroke(DASH);
            g2.drawRoundRect(x, y, CARD_W - 1, CHAMP_H - 1, ARC, ARC);
            g2.setStroke(ONE);
        }
        java.awt.Color ink = champDecided ? Theme.ON_PRIMARY : Theme.PRIMARY;
        glyph(g2, TROPHY_PATH, x + (CARD_W - TROPHY) / 2, y + Theme.SPACE_L, TROPHY, ink);
        if (champDecided) {
            g2.setFont(KICKER_FONT);
            g2.setColor(ink);
            g2.drawString("Champion", champKickerX, y + Theme.SPACE_L + TROPHY + 20);
        }
        g2.setFont(CHAMP_FONT);
        g2.setColor(ink);
        g2.drawString(champText, champTextX, y + CHAMP_H - Theme.SPACE_L - 4);
    }

    /** Draws a prebuilt 24-unit glyph at size px without creating objects. */
    private static void glyph(Graphics2D g2, Path2D path, int x, int y, int size, java.awt.Color color) {
        double scale = size / 24.0;
        Stroke before = g2.getStroke();
        g2.setColor(color);
        g2.translate(x, y);
        g2.scale(scale, scale);
        g2.setStroke(GLYPH_STROKE);
        g2.draw(path);
        g2.scale(1 / scale, 1 / scale);
        g2.translate(-x, -y);
        g2.setStroke(before);
    }
}
