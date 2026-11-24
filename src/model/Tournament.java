package model;

import algo.Rounds;
import algo.Shuffle;
import algo.Sorts;
import ds.MatchStore;
import ds.PlayerStore;
import ds.ReadyQueue;
import ds.ResultStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Tournament rules. The UI calls these methods and never edits Player or Match directly. */
public class Tournament {
    public String name;
    public SportMode sport = SportMode.ESPORTS;

    private final PlayerStore players = new PlayerStore();
    private final MatchStore matches = new MatchStore();
    private final ResultStack results = new ResultStack();
    private final ReadyQueue ready = new ReadyQueue();
    private int nextMatchId = 1;
    private boolean generated;
    private int totalRounds;
    private Match finalMatch;

    public Tournament(String name) {
        this.name = name;
    }

    public boolean isGenerated() { return generated; }
    public PlayerStore players() { return players; }
    public MatchStore matches() { return matches; }
    public ResultStack results() { return results; }
    public ReadyQueue ready() { return ready; }

    // ----- Players (locked once the bracket exists) -----

    public Player addPlayer(String playerName) {
        requireUnlocked();
        String clean = cleanName(playerName);
        if (players.findByName(clean) != null) throw new IllegalArgumentException("A player named " + clean + " already exists");
        return players.add(clean);
    }

    public void renamePlayer(Player p, String playerName) {
        requireUnlocked();
        String clean = cleanName(playerName);
        Player other = players.findByName(clean);
        if (other != null && other != p) throw new IllegalArgumentException("A player named " + clean + " already exists");
        p.name = clean;
    }

    public void removePlayer(Player p) {
        requireUnlocked();
        players.remove(p);
    }

    // ----- Bracket -----

    public void generateBracket() {
        generateBracket(new Random());
    }

    /** Pass a fixed Random in tests to get the same bracket every run. */
    public void generateBracket(Random rnd) {
        if (generated) throw new IllegalStateException("The bracket already exists");
        int n = players.size();
        if (!Rounds.isBracketSize(n)) {
            throw new IllegalArgumentException("A bracket needs 4, 8 or 16 players. You have " + n + ".");
        }
        List<Player> seeds = Shuffle.shuffled(new ArrayList<>(players.list()), rnd);
        totalRounds = Rounds.rounds(n);

        // Create every match now, round by round, so IDs follow bracket order (M001..).
        Match[][] byRound = new Match[totalRounds + 1][];
        for (int r = 1; r <= totalRounds; r++) {
            int count = n >> r;
            byRound[r] = new Match[count];
            for (int i = 0; i < count; i++) {
                Match m = new Match(nextMatchId++, r);
                m.bracket = true;
                byRound[r][i] = m;
                matches.add(m);
            }
        }
        // Link each match to the match its winner goes to.
        for (int r = 1; r < totalRounds; r++) {
            for (int i = 0; i < byRound[r].length; i++) {
                byRound[r][i].next = byRound[r + 1][i / 2];
                byRound[r][i].nextSlot = i % 2;
            }
        }
        // Seed round one. Matches with two players are ready to play.
        for (int i = 0; i < n / 2; i++) {
            Match m = byRound[1][i];
            m.a = seeds.get(2 * i);
            m.b = seeds.get(2 * i + 1);
            m.status = Match.Status.READY;
            ready.offer(m);
        }
        finalMatch = byRound[totalRounds][0];
        generated = true;
    }

    // ----- Results -----

    /** Records a winner, updates points, and moves the winner into the next match. */
    public void setWinner(Match m, Player winner) {
        if (m == null) throw new IllegalArgumentException("Match not found");
        if (m.status == Match.Status.PLAYED) throw new IllegalStateException(matchId(m) + " already has a winner");
        if (m.a == null || m.b == null) throw new IllegalStateException(matchId(m) + " is not ready yet");
        if (winner != m.a && winner != m.b) {
            throw new IllegalArgumentException("The winner must be " + m.a.name + " or " + m.b.name);
        }
        Player loser = winner == m.a ? m.b : m.a;
        results.push(new ResultStack.Entry(m, m.status, winner, loser));
        winner.wins++;
        winner.points++;
        loser.losses++;
        ready.remove(m);
        m.winner = winner;
        m.status = Match.Status.PLAYED;

        Match next = m.next;
        if (next != null) {
            if (m.nextSlot == 0) next.a = winner;
            else next.b = winner;
            if (next.a != null && next.b != null && next.status == Match.Status.WAITING) {
                next.status = Match.Status.READY;
                ready.offer(next);
            }
        }
    }

    /** Reverses the newest result. Returns the match that was reversed. */
    public Match undo() {
        if (results.isEmpty()) throw new IllegalStateException("Nothing to undo");
        ResultStack.Entry e = results.peek();
        Match m = e.match;
        Match next = m.next;
        if (next != null && (next.status == Match.Status.PLAYED || next.status == Match.Status.IN_PROGRESS)) {
            throw new IllegalStateException("Undo the result of " + matchId(next) + " first");
        }
        results.pop();
        if (next != null) {
            ready.remove(next);
            if (m.nextSlot == 0) next.a = null;
            else next.b = null;
            next.status = Match.Status.WAITING;
        }
        e.winner.wins--;
        e.winner.points--;
        e.loser.losses--;
        m.winner = null;
        m.status = e.prevStatus;
        if (m.status == Match.Status.READY) ready.offer(m);
        return m;
    }

    /** Takes the first ready match and marks it in progress. Returns null when none is ready. */
    public Match startNext() {
        Match m = ready.poll();
        if (m != null) m.status = Match.Status.IN_PROGRESS;
        return m;
    }

    // ----- Extra matches (Matches page Add/Delete; they do not feed the bracket) -----

    public Match addExtraMatch(int round, Player a, Player b) {
        if (round < 1) throw new IllegalArgumentException("Round must be 1 or more");
        if (a == null || b == null || a == b) throw new IllegalArgumentException("Pick two different players");
        Match m = new Match(nextMatchId++, round);
        m.a = a;
        m.b = b;
        m.status = Match.Status.READY;
        matches.add(m);
        ready.offer(m);
        return m;
    }

    public void removeMatch(Match m) {
        if (m.bracket) throw new IllegalStateException("Bracket matches cannot be deleted");
        if (m.status == Match.Status.PLAYED || m.status == Match.Status.IN_PROGRESS) {
            throw new IllegalStateException("Only unplayed matches can be deleted");
        }
        matches.remove(m);
        ready.remove(m);
    }

    // ----- Views -----

    public Player champion() {
        return finalMatch != null && finalMatch.status == Match.Status.PLAYED ? finalMatch.winner : null;
    }

    /** Lowest bracket round with an unplayed match. Equals the last round once the final is played. */
    public int currentRound() {
        if (!generated) return 0;
        int best = totalRounds;
        for (Match m : matches.list()) {
            if (m.bracket && m.status != Match.Status.PLAYED && m.round < best) best = m.round;
        }
        return best;
    }

    public Match nextMatch() {
        return ready.peek();
    }

    /** Players by points, highest first. Ties sorted by name. */
    public List<Player> standings() {
        List<Player> copy = new ArrayList<>(players.list());
        Sorts.byPoints(copy);
        return copy;
    }

    /** Matches by round, lowest first (merge sort). */
    public List<Match> matchesByRound() {
        return Sorts.mergeSort(new ArrayList<>(matches.list()), (x, y) -> Integer.compare(x.round, y.round));
    }

    public Match findMatch(String text) {
        int id = parseId(text, 'M');
        return id < 0 ? null : matches.findById(id);
    }

    public Player findPlayerById(String text) {
        int id = parseId(text, 'P');
        return id < 0 ? null : players.findById(id);
    }

    // ----- IDs: displayed as P001 and M013; input is case-insensitive -----

    public static String playerId(Player p) {
        return formatId('P', p.id);
    }

    public static String matchId(Match m) {
        return formatId('M', m.id);
    }

    public static String formatId(char prefix, int id) {
        return String.format("%c%03d", prefix, id);
    }

    /** Returns the number after the prefix, or -1 if the text is not exactly prefix + 3 digits. */
    public static int parseId(String text, char prefix) {
        if (text == null) return -1;
        String t = text.trim().toUpperCase();
        if (t.length() != 4 || t.charAt(0) != Character.toUpperCase(prefix)) return -1;
        String digits = t.substring(1);
        if (!digits.chars().allMatch(Character::isDigit)) return -1;
        return Integer.parseInt(digits);
    }

    // ----- helpers -----

    private void requireUnlocked() {
        if (generated) throw new IllegalStateException("Players are locked once the bracket exists");
    }

    private static String cleanName(String s) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty");
        return s.trim();
    }
}
