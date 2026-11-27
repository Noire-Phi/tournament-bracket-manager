package test;

import algo.Rounds;
import algo.Searches;
import algo.Shuffle;
import algo.Sorts;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import model.Match;
import model.Player;
import model.Tournament;

/** Plain checks, no JUnit. Run with: java -cp out test.TournamentTest */
public class TournamentTest {
    private static int passed = 0, failed = 0;

    public static void main(String[] args) {
        testRounds();
        testSearches();
        testSorts();
        testBracketSizes();
        testFullRun();
        testStartNextAndUndo();
        testRejects();
        testIds();
        testExtraMatches();

        System.out.println(passed + " checks passed, " + failed + " failed");
        if (failed == 0) System.out.println("All checks passed");
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
        } else {
            failed++;
            System.out.println("FAILED: " + name);
        }
    }

    private static boolean throwsError(Runnable r) {
        try {
            r.run();
            return false;
        } catch (IllegalArgumentException | IllegalStateException e) {
            return true;
        }
    }

    private static Tournament tournamentWith(int n, long seed) {
        Tournament t = new Tournament("Test");
        for (int i = 1; i <= n; i++) t.addPlayer("Player " + i);
        t.generateBracket(new Random(seed));
        return t;
    }

    private static void testRounds() {
        check("rounds(1)=0", Rounds.rounds(1) == 0);
        check("rounds(4)=2", Rounds.rounds(4) == 2);
        check("rounds(8)=3", Rounds.rounds(8) == 3);
        check("rounds(16)=4", Rounds.rounds(16) == 4);
        check("size 16 allowed", Rounds.isBracketSize(16));
        check("size 5 rejected", !Rounds.isBracketSize(5));
        check("size 32 rejected", !Rounds.isBracketSize(32));
    }

    private static void testSearches() {
        Player venti = new Player(1, "Venti");
        Player klee = new Player(2, "Klee");
        List<Player> players = Arrays.asList(venti, klee);
        check("linear search finds Klee ignoring case", Searches.linearByName(players, "klee") == klee);
        check("linear search misses unknown name", Searches.linearByName(players, "Nobody") == null);
        check("contains search keeps matches in order", Searches.linearContains(players, "E").size() == 2
                && Searches.linearContains(players, "  ent ").size() == 1
                && Searches.linearContains(players, "").size() == 2
                && Searches.linearContains(players, "zz").isEmpty());

        List<Match> byId = new ArrayList<>();
        for (int id : new int[] {1, 3, 5, 9}) byId.add(new Match(id, 1));
        check("binary search finds M005", Searches.binaryById(byId, 5).id == 5);
        check("binary search finds M001", Searches.binaryById(byId, 1).id == 1);
        check("binary search misses M004", Searches.binaryById(byId, 4) == null);
    }

    private static void testSorts() {
        Player ann = new Player(1, "Ann"); ann.points = 3;
        Player bo = new Player(2, "Bo"); bo.points = 5;
        Player cy = new Player(3, "Cy"); cy.points = 3;
        Player dee = new Player(4, "Dee"); dee.points = 0;
        Player eve = new Player(5, "Eve"); eve.points = 5;
        List<Player> list = new ArrayList<>(Arrays.asList(ann, cy, dee, eve, bo));
        Sorts.byPoints(list);
        List<String> names = new ArrayList<>();
        for (Player p : list) names.add(p.name);
        check("points sort with name ties", names.equals(Arrays.asList("Bo", "Eve", "Ann", "Cy", "Dee")));
        // Bo 5, Eve 5, Ann 3, Cy 3, Dee 0: shared ranks 1, 1, 3, 3, 5
        check("tied players share a rank", Arrays.equals(algo.Ranks.of(list), new int[] {1, 1, 3, 3, 5}));
        check("rank chip groups", algo.Ranks.group(list, 1) == algo.Ranks.LEADERS
                && algo.Ranks.group(list, 2) == algo.Ranks.NEXT && algo.Ranks.group(list, 4) == algo.Ranks.REST);

        Match m1 = new Match(1, 2), m2 = new Match(2, 1), m3 = new Match(3, 3), m4 = new Match(4, 1);
        List<Match> sorted = Sorts.mergeSort(new ArrayList<>(Arrays.asList(m1, m2, m3, m4)),
                (x, y) -> Integer.compare(x.round, y.round));
        check("merge sort by round", sorted.get(0) == m2 && sorted.get(1) == m4 && sorted.get(2) == m1 && sorted.get(3) == m3);

        List<Integer> nums = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        List<Integer> shuffled = Shuffle.shuffled(nums, new Random(42));
        check("shuffle keeps every item", shuffled.size() == 6 && shuffled.containsAll(nums));
    }

    private static void testBracketSizes() {
        for (int n : new int[] {4, 8, 16}) {
            Tournament t = tournamentWith(n, 1);
            check(n + " players: " + (n - 1) + " matches", t.matches().size() == n - 1);
            check(n + " players: " + (n / 2) + " ready in round one", t.ready().size() == n / 2);
            check(n + " players: no champion yet", t.champion() == null);
        }
        Tournament five = new Tournament("Five");
        for (int i = 1; i <= 5; i++) five.addPlayer("P" + i);
        check("5 players rejected", throwsError(() -> five.generateBracket(new Random(1))));
        Tournament thirtyTwo = new Tournament("Big");
        for (int i = 1; i <= 32; i++) thirtyTwo.addPlayer("P" + i);
        check("32 players rejected", throwsError(() -> thirtyTwo.generateBracket(new Random(1))));
    }

    private static void testFullRun() {
        Tournament t = tournamentWith(16, 7);
        int results = 0;
        while (t.champion() == null) {
            Match m = t.startNext();
            if (m == null) {
                check("queue never empties before a champion", false);
                return;
            }
            t.setWinner(m, m.a);
            results++;
        }
        check("16 players need 15 results", results == 15);
        check("champion has 4 points", t.champion().points == 4);
        int total = 0;
        for (Player p : t.players().list()) total += p.points;
        check("total points equal matches played", total == 15);
        check("current round is last round after final", t.currentRound() == 4);
        check("round names for 16 players", t.roundName(1, true).equals("Round of 16") && t.roundName(2, true).equals("Quarterfinals")
                && t.roundName(3, false).equals("Semifinal") && t.roundName(4, true).equals("Final"));
        int out = 0;
        for (Player p : t.players().list()) if (t.isOut(p)) out++;
        check("15 players are out and the champion is still in", out == 15 && !t.isOut(t.champion()));
    }

    private static void testStartNextAndUndo() {
        Tournament t = tournamentWith(8, 3);
        Match first = t.ready().peek();
        Player winner = first.a;
        t.setWinner(first, winner);
        check("winner gets 1 point", winner.points == 1);
        check("ready queue shrinks after result", t.ready().size() == 3);
        Match next = first.next;
        check("winner moved into next match slot",
                (first.nextSlot == 0 ? next.a : next.b) == winner);

        t.undo();
        check("undo removes the point", winner.points == 0 && winner.wins == 0);
        check("undo clears the next slot", first.nextSlot == 0 ? next.a == null : next.b == null);
        check("undo re-queues the match", first.status == Match.Status.READY && t.ready().size() == 4);

        // Undo is blocked while the next match is in progress.
        Match a = t.ready().peek();
        t.setWinner(a, a.a);
        Match sibling = null;
        for (Match m : t.matches().list()) {
            if (m != a && m.next == a.next) sibling = m;
        }
        t.setWinner(sibling, sibling.a);
        Match shared = a.next;
        check("shared next match is ready", shared.status == Match.Status.READY);
        Match started = null;
        while (started != shared) started = t.startNext();
        check("shared next match in progress", shared.status == Match.Status.IN_PROGRESS);
        check("undo blocked while next match in progress", throwsError(t::undo));
    }

    private static void testRejects() {
        Tournament t = tournamentWith(8, 5);
        Match m = t.ready().peek();
        Player outsider = null;
        for (Player p : t.players().list()) {
            if (p != m.a && p != m.b) { outsider = p; break; }
        }
        Player outsiderRef = outsider;
        check("winner must be one of the two players", throwsError(() -> t.setWinner(m, outsiderRef)));
        t.setWinner(m, m.a);
        check("second winner for a match is rejected", throwsError(() -> t.setWinner(m, m.a)));
        check("missing match is rejected", throwsError(() -> t.setWinner(null, outsiderRef)));
        check("adding a player after the bracket is rejected", throwsError(() -> t.addPlayer("Late")));
        check("duplicate player name is rejected", throwsError(() -> new Tournament("X").addPlayer("  ")));

        Tournament dup = new Tournament("Dup");
        dup.addPlayer("Venti");
        check("duplicate name ignoring case is rejected", throwsError(() -> dup.addPlayer("venti")));
    }

    private static void testIds() {
        check("p001 parses to 1", Tournament.parseId("p001", 'P') == 1);
        check("M0013 is not a valid ID", Tournament.parseId("M0013", 'M') == -1);
        check("M013 parses to 13", Tournament.parseId("M013", 'M') == 13);
        check("X013 is not a match ID", Tournament.parseId("X013", 'M') == -1);
        check("format M013", Tournament.formatId('M', 13).equals("M013"));

        Tournament t = tournamentWith(16, 9);
        check("find M013 in a 16-player bracket", t.findMatch("m013") != null && t.findMatch("M013").id == 13);
        check("find M0013 reports not found", t.findMatch("M0013") == null);
        check("find M016 reports not found", t.findMatch("M016") == null);
    }

    private static void testExtraMatches() {
        Tournament t = new Tournament("Extras");
        Player a = t.addPlayer("Alpha");
        Player b = t.addPlayer("Beta");
        Match extra = t.addExtraMatch(1, a, b);
        check("extra match is not a bracket match", !extra.bracket);
        t.setWinner(extra, a);
        check("extra match result gives a point", a.points == 1);
        check("cannot delete a played extra match", throwsError(() -> t.removeMatch(extra)));
        check("winner of an extra match is recorded", extra.winner == a);
        check("results stack holds the extra match", t.results().peek().match == extra);
        t.undo();
        Match unplayed = t.addExtraMatch(2, a, b);
        t.removeMatch(unplayed);
        check("unplayed extra match can be deleted", t.matches().size() == 1);
    }
}
